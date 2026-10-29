import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { detailVehicle } from './vehicle-detail/vehicle-detail.test-support.spec';
import { Vehicle } from './vehicle.model';
import { VehicleService } from './vehicle.service';

describe('Issue #3: complete records are available to the list and detail', () => {
  it('requests all events for the detail UUID using an encoded query value', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const service = TestBed.inject(VehicleService);
    const http = TestBed.inject(HttpTestingController);
    const events = [detailVehicle(), detailVehicle({ status: 'DEREGISTERED' })];
    let received: Vehicle[] | undefined;
    service.list(0, events[0].uuid).subscribe(rows => received = rows);
    http.expectOne(`http://localhost:8080/api/vehicles?page=0&uuid=${events[0].uuid}`).flush(events);
    expect(received).toEqual(events);
    http.verify();
  });

  it('preserves all fields and separate events with the same UUID from the list response', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const service = TestBed.inject(VehicleService);
    const http = TestBed.inject(HttpTestingController);
    const records = [detailVehicle(), detailVehicle({ status: 'DEREGISTERED',
      startDate: '2026-05-09', time: '18:45:06', userUuid: '22222222-2222-4222-8222-222222222222' })];
    let received: Vehicle[] | undefined;
    service.list(2).subscribe(rows => received = rows);
    const request = http.expectOne('http://localhost:8080/api/vehicles?page=2');
    expect(request.request.method).toBe('GET');
    request.flush(records);
    expect(received).toEqual(records);
    expect(received?.length).toBe(2);
    expect(Object.keys(received![0]).sort()).toEqual([
      'uuid', 'status', 'startDate', 'endDate', 'time', 'userUuid', 'brand', 'type', 'category',
    ].sort());
    http.verify();
  });
});
