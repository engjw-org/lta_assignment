import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Vehicle } from '../vehicle.model';
import { VehicleService } from '../vehicle.service';
import { VehicleListComponent } from './vehicle-list.component';

describe('Issue #1: list start-date display (R-2.3)', () => {
  let fixture: ComponentFixture<VehicleListComponent>;
  let service: jasmine.SpyObj<VehicleService>;

  beforeEach(async () => {
    service = jasmine.createSpyObj<VehicleService>('VehicleService', ['list']);
    await TestBed.configureTestingModule({
      imports: [VehicleListComponent],
      providers: [{ provide: VehicleService, useValue: service }],
    }).compileComponents();
    fixture = TestBed.createComponent(VehicleListComponent);
  });

  for (const [input, expected] of [
    ['2026-04-08', '08 Apr 2026'],
    ['2022-11-01', '01 Nov 2022'],
  ]) {
    it(`renders ${input} as ${expected}`, () => {
      const vehicle: Vehicle = {
        uuid: '00000000-0000-4000-8000-000000000001',
        status: 'REGISTERED',
        startDate: input,
        endDate: '2032-01-01',
        time: '09:00:00',
        userUuid: '11111111-1111-4111-8111-111111111111',
        brand: 'Toyota',
        type: '5-seat',
        category: 'personal',
      };
      service.list.and.returnValue(of([vehicle]));

      fixture.detectChanges();

      const table: HTMLTableElement = fixture.nativeElement.querySelector('table');
      const dateColumn = Array.from(table.querySelectorAll('thead th'))
        .findIndex((heading) => heading.textContent?.trim() === 'Start Date');
      expect(dateColumn).toBeGreaterThanOrEqual(0);
      const cell = table.querySelectorAll('tbody tr')[0]?.querySelectorAll('td')[dateColumn];
      expect(cell?.textContent?.trim()).toBe(expected);
      expect(vehicle.startDate).toBe(input);
    });
  }
});
