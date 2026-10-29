import { provideLocationMocks } from '@angular/common/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AppComponent } from '../app.component';
import { appConfig } from '../app.config';
import { Vehicle } from '../vehicle.model';
import { VehicleService } from '../vehicle.service';
import { VehicleDetailComponent } from './vehicle-detail.component';
import { detailVehicle, visibleText } from './vehicle-detail.test-support.spec';

// Test-only contract permits a real red run without changing the production model first.
type AuditedVehicle = Vehicle & {
  audit: { sourceFile: string; sourceRow: number; ingestedAt: string };
};
const audited = (sourceRow = 2): AuditedVehicle => ({
  ...detailVehicle(), audit: { sourceFile: 'personal.csv', sourceRow, ingestedAt: '2026-10-29T12:34:56Z' },
});

describe('Issue #7: audit details (R-6.4)', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [VehicleDetailComponent] }).compileComponents();
  });

  it('shows labelled source file, physical row and ingestion time with an explicit timezone', () => {
    const fixture = TestBed.createComponent(VehicleDetailComponent);
    fixture.componentRef.setInput('vehicle', audited(37));
    fixture.detectChanges();
    const text = visibleText(fixture.nativeElement);
    for (const label of ['Source file', 'Source row', 'Ingested at']) {
      expect(text.toLowerCase()).withContext(`audit label: ${label}`).toContain(label.toLowerCase());
    }
    for (const value of ['personal.csv', '37', '29 Oct 2026', '12:34:56', 'UTC']) {
      expect(text).withContext(`audit value: ${value}`).toContain(value);
    }
  });

  it('renders source metadata as text and never creates injected HTML (R-6.2)', () => {
    const vehicle = audited();
    vehicle.audit.sourceFile = '<img data-audit-injection="yes" src=x onerror=alert(1)>';
    const fixture = TestBed.createComponent(VehicleDetailComponent);
    fixture.componentRef.setInput('vehicle', vehicle);
    fixture.detectChanges();
    expect(visibleText(fixture.nativeElement)).toContain(vehicle.audit.sourceFile);
    expect(fixture.nativeElement.querySelector('[data-audit-injection]')).toBeNull();
  });
});

describe('Issue #7: audit identity survives popup refresh', () => {
  const key = 'vrls2.selected-event';
  let service: jasmine.SpyObj<VehicleService>;
  const first = audited(2);
  const second = audited(3); // All vehicle fields are identical; these are distinct source rows.

  beforeEach(async () => {
    sessionStorage.removeItem(key);
    service = jasmine.createSpyObj<VehicleService>('VehicleService', ['list']);
    service.list.and.returnValue(of([first, second]));
    await TestBed.configureTestingModule({ imports: [AppComponent], providers: [
      ...appConfig.providers, provideLocationMocks(), { provide: VehicleService, useValue: service },
    ] }).compileComponents();
  });

  afterEach(() => sessionStorage.removeItem(key));

  async function visit() {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    await TestBed.inject(Router).navigateByUrl(`/${first.uuid}`);
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture;
  }

  it('restores the exact duplicate event by audit values after JSON round-trip', async () => {
    sessionStorage.setItem(key, JSON.stringify(second));
    const fixture = await visit();
    const detail = fixture.nativeElement.querySelector('app-vehicle-detail') as HTMLElement | null;
    expect(detail).withContext('nested audit objects must match by values, not object identity').not.toBeNull();
    if (!detail) return; // The failed assertion above remains a failure.
    expect(visibleText(detail)).toContain('personal.csv');
    const chooser: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    expect(chooser.value).toBe('1');
    expect(JSON.parse(sessionStorage.getItem(key)!)).toEqual(second);
  });

  for (const replacement of [
    { sourceFile: 'untrusted.csv' }, { sourceRow: 999 }, { ingestedAt: '2020-01-01T00:00:00Z' },
  ]) {
    it(`rejects changed cached audit ${Object.keys(replacement)[0]} and asks for an event`, async () => {
      sessionStorage.setItem(key, JSON.stringify({ ...second, audit: { ...second.audit, ...replacement } }));
      const fixture = await visit();
      expect(fixture.nativeElement.querySelector('app-vehicle-detail')).toBeNull();
      expect(fixture.nativeElement.querySelector('select')).not.toBeNull();
      expect(fixture.nativeElement.querySelector('select').value).toBe('');
    });
  }

  it('renders the selected API event rather than trusting cached metadata', async () => {
    sessionStorage.setItem(key, JSON.stringify({ ...second, audit: { ...second.audit, sourceRow: 999 } }));
    const fixture = await visit();
    const chooser: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    chooser.value = '1';
    chooser.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(JSON.parse(sessionStorage.getItem(key)!)).toEqual(second);
    expect(visibleText(fixture.nativeElement)).not.toContain('999');
  });
});
