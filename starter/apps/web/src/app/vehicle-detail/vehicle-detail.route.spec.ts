import { provideLocationMocks } from '@angular/common/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
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
