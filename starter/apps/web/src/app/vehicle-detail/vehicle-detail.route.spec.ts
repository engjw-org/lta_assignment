import { provideLocationMocks } from '@angular/common/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AppComponent } from '../app.component';
import { appConfig } from '../app.config';
import { VehicleService } from '../vehicle.service';
import { detailVehicle, visibleText } from './vehicle-detail.test-support.spec';

describe('Issue #3: detail URL uses the actual application routing', () => {
  it('renders a complete unique vehicle at /uuid instead of the list or TODO', async () => {
    const vehicle = detailVehicle();
    const service = jasmine.createSpyObj<VehicleService>('VehicleService', ['list', 'getOne']);
    service.list.and.returnValue(of([vehicle]));
    service.getOne.and.returnValue(of(vehicle));
    await TestBed.configureTestingModule({ imports: [AppComponent], providers: [
      ...appConfig.providers, provideLocationMocks(), { provide: VehicleService, useValue: service },
    ] }).compileComponents();
    // Do not install test-only routes: the production application must provide them.
    const router = TestBed.inject(Router, null);
    expect(router?.config.length ?? 0)
      .withContext('the application must configure routes, including /uuid').toBeGreaterThan(0);
    if (!router || router.config.length === 0) return;
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    await router.navigateByUrl(`/${vehicle.uuid}`);
    await fixture.whenStable();
    fixture.detectChanges();
    const root: HTMLElement = fixture.nativeElement;
    expect(router.url).toBe(`/${vehicle.uuid}`);
    expect(root.querySelector('app-vehicle-list')).withContext('popup URL is a detail page').toBeNull();
    const text = visibleText(root);
    for (const value of [vehicle.uuid, vehicle.userUuid, vehicle.brand, vehicle.type,
      vehicle.status, vehicle.category, vehicle.time!, '08 Apr 2026', '01 Nov 2032']) {
      expect(text.toLowerCase()).toContain(value.toLowerCase());
    }
    expect(text).not.toContain('TODO');
  });
});

describe('Issue #3: selected event handoff and direct-link recovery', () => {
  const storageKey = 'vrls2.selected-event';
  const first = detailVehicle();
  const second = detailVehicle({ status: 'DEREGISTERED', startDate: '2026-05-09', time: '18:45:06' });
  let service: jasmine.SpyObj<VehicleService>;

  beforeEach(async () => {
    sessionStorage.removeItem(storageKey);
    service = jasmine.createSpyObj<VehicleService>('VehicleService', ['list', 'getOne']);
    service.list.and.returnValue(of([first, second]));
    await TestBed.configureTestingModule({ imports: [AppComponent], providers: [
      ...appConfig.providers, provideLocationMocks(), { provide: VehicleService, useValue: service },
    ] }).compileComponents();
  });

  afterEach(() => sessionStorage.removeItem(storageKey));

  async function visit(uuid = first.uuid) {
    const router = TestBed.inject(Router, null);
    expect(router?.config.length ?? 0).withContext('production routes exist').toBeGreaterThan(0);
    if (!router || !router.config.length) return null;
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    await router.navigateByUrl(`/${uuid}`);
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture;
  }

  it('asks which event to show for a direct URL and shows the chosen event', async () => {
    const fixture = await visit();
    if (!fixture) return;
    const root: HTMLElement = fixture.nativeElement;
    expect(root.querySelector('app-vehicle-detail')).toBeNull();
    const select = root.querySelector('select');
    expect(select).withContext('event chooser, not an arbitrary first match').not.toBeNull();
    if (!select) return;
    expect(select.options.length).toBe(3);
    select.value = select.options[2].value;
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(visibleText(root.querySelector('app-vehicle-detail')!)).toContain('18:45:06');
    expect(service.list.calls.mostRecent().args as unknown[]).toEqual([0, first.uuid]);
  });

  it('retains the exact clicked event across reloads without mutating the supplied values', async () => {
    sessionStorage.setItem(storageKey, JSON.stringify(second));
    const fixture = await visit();
    if (!fixture) return;
    const text = visibleText(fixture.nativeElement.querySelector('app-vehicle-detail'));
    expect(text).toContain('09 May 2026');
    expect(text).toContain('18:45:06');
    expect(text).not.toContain('08 Apr 2026');
    expect(JSON.parse(sessionStorage.getItem(storageKey)!)).toEqual(second);
  });

  for (const cached of ['{bad json', JSON.stringify(detailVehicle({ brand: '<img src=x onerror=alert(1)>' }))]) {
    it('does not display a corrupt or changed cached record as validated vehicle data', async () => {
      sessionStorage.setItem(storageKey, cached);
      const fixture = await visit();
      if (!fixture) return;
      expect(fixture.nativeElement.querySelector('app-vehicle-detail')).toBeNull();
      expect(fixture.nativeElement.querySelector('select')).not.toBeNull();
      expect(visibleText(fixture.nativeElement)).not.toContain('<img');
    });
  }

  it('shows a useful message when the vehicle is missing', async () => {
    service.list.and.returnValue(of([]));
    const fixture = await visit();
    if (fixture) expect(visibleText(fixture.nativeElement)).toMatch(/not found/i);
  });

  it('shows a useful message on API failure without exposing server details', async () => {
    service.list.and.returnValue(throwError(() => new Error('/private/server/path stack trace')));
    const fixture = await visit();
    if (!fixture) return;
    expect(visibleText(fixture.nativeElement)).toMatch(/unable|could not/i);
    expect(visibleText(fixture.nativeElement)).not.toContain('/private/server/path');
  });

  it('rejects an invalid UUID without an API call', async () => {
    const fixture = await visit('invalid-uuid');
    if (!fixture) return;
    expect(visibleText(fixture.nativeElement)).toMatch(/invalid/i);
    expect(service.list).not.toHaveBeenCalled();
  });

  it('closes the browser window from the detail Close button', async () => {
    const close = spyOn(window, 'close');
    sessionStorage.setItem(storageKey, JSON.stringify(second));
    const fixture = await visit();
    if (!fixture) return;
    const button = Array.from(fixture.nativeElement.querySelectorAll('button'))
      .find(element => /close/i.test((element as HTMLElement).textContent ?? '')) as HTMLButtonElement;
    expect(button).toBeDefined();
    button?.click();
    expect(close).toHaveBeenCalledTimes(1);
  });
});
