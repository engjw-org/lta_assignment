import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { detailVehicle } from '../vehicle-detail/vehicle-detail.test-support.spec';
import { Vehicle } from '../vehicle.model';
import { VehicleService } from '../vehicle.service';
import { VehicleListComponent } from './vehicle-list.component';

// Issue #5 covers supplied list records. Full-dataset API queries and the
// proposed 2,000-record limit are deferred; these are not performance tests.
describe('Issue #5: UUID-first order and separate CSV date filters', () => {
  let fixture: ComponentFixture<VehicleListComponent>;
  let service: jasmine.SpyObj<VehicleService>;
  const lowUuid = '00000000-0000-4000-8000-000000000001';
  const highUuid = '00000000-0000-4000-8000-000000000002';

  beforeEach(async () => {
    service = jasmine.createSpyObj<VehicleService>('VehicleService', ['list']);
    await TestBed.configureTestingModule({
      imports: [VehicleListComponent],
      providers: [{ provide: VehicleService, useValue: service }],
    }).compileComponents();
    fixture = TestBed.createComponent(VehicleListComponent);
  });

  function render(records: Vehicle[]): void {
    service.list.and.returnValue(of(records));
    fixture.detectChanges();
  }

  function displayedBrands(): string[] {
    const root = fixture.nativeElement as HTMLElement;
    return Array.from(root.querySelectorAll('tbody tr')).map(row =>
      row.querySelectorAll('td')[1]?.textContent?.trim() ?? '',
    );
  }

  // Exercise labelled DOM controls, without assuming new component methods.
  async function setDate(field: 'start' | 'end', value: string): Promise<boolean> {
    const root = fixture.nativeElement as HTMLElement;
    const label = new RegExp(`^${field}\\s+date\\b`, 'i');
    const input = Array.from(root.querySelectorAll('input')).find(candidate => {
      const accessibleName = candidate.getAttribute('aria-label') ??
        Array.from(candidate.labels ?? []).map(item => item.textContent?.trim()).join(' ');
      return label.test(accessibleName);
    });
    expect(input).withContext(`missing labelled ${field} date filter`).toBeDefined();
    if (!input) return false; // Keep the red failure focused on the missing feature.
    expect(input.type).withContext('CSV start_date/end_date have date-only precision').toBe('date');
    input.value = value;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    input.dispatchEvent(new Event('change', { bubbles: true }));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    return true;
  }

  it('orders by the full UUID first, even when another UUID has a newer start date (R-2.1 over R-1.4)', () => {
    const newer = detailVehicle({ uuid: highUuid, startDate: '2026-12-31', brand: 'Alpha' });
    const older = detailVehicle({ uuid: lowUuid, startDate: '2024-01-01', brand: 'Zulu' });
    const records = [newer, older];
    const original = JSON.stringify(records);
    render(records);

    // The first eight UUID characters are identical: sorting the displayed
    // abbreviation, dates globally or brand alphabetically would all be wrong.
    expect(displayedBrands()).toEqual(['Zulu', 'Alpha']);
    expect(JSON.stringify(records)).withContext('do not rewrite the supplied events').toBe(original);
  });

  it('orders repeated-UUID events by start date descending across months and years without losing history', () => {
    const records = [
      detailVehicle({ uuid: lowUuid, startDate: '2025-12-31', endDate: '2039-01-01', brand: 'Oldest' }),
      detailVehicle({ uuid: lowUuid, startDate: '2026-02-01', endDate: '2030-01-01', brand: 'Newest', status: 'DEREGISTERED' }),
      detailVehicle({ uuid: lowUuid, startDate: '2026-01-31', endDate: '2035-01-01', brand: 'Middle' }),
    ];
    const original = JSON.stringify(records);
    render(records);

    expect(displayedBrands()).toEqual(['Newest', 'Middle', 'Oldest']);
    expect(JSON.stringify(records)).toBe(original);
  });

  it('uses start_date as an inclusive lower bound, independently of end_date and time', async () => {
    render([
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-07', endDate: '2040-01-01', time: '23:59:59', brand: 'Before' }),
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-08', endDate: '2039-01-01', time: '00:00:00', brand: 'At bound' }),
      detailVehicle({ uuid: highUuid, startDate: '2026-04-09', endDate: '2038-01-01', time: '00:00:00', brand: 'After' }),
    ]);
    if (!await setDate('start', '2026-04-08')) return;

    expect(displayedBrands()).toEqual(['At bound', 'After']);
  });

  it('uses end_date as an inclusive upper bound rather than comparing start_date to both controls', async () => {
    render([
      detailVehicle({ uuid: lowUuid, startDate: '2020-01-01', endDate: '2032-11-01', brand: 'At bound' }),
      detailVehicle({ uuid: lowUuid, startDate: '2020-01-01', endDate: '2032-11-02', brand: 'After' }),
      detailVehicle({ uuid: highUuid, startDate: '2020-01-01', endDate: '2032-10-31', brand: 'Before' }),
    ]);
    if (!await setDate('end', '2032-11-01')) return;

    expect(displayedBrands()).toEqual(['At bound', 'Before']);
  });

  it('combines both date conditions and the selected status, retaining UUID/date order', async () => {
    render([
      detailVehicle({ uuid: highUuid, startDate: '2026-04-08', endDate: '2032-11-01', brand: 'Higher UUID' }),
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-07', endDate: '2032-11-01', brand: 'Start too early' }),
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-09', endDate: '2032-11-02', brand: 'End too late' }),
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-09', endDate: '2032-11-01', brand: 'Wrong status', status: 'DEREGISTERED' }),
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-08', endDate: '2032-11-01', brand: 'Older match' }),
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-09', endDate: '2032-11-01', brand: 'Newer match' }),
    ]);
    if (!await setDate('start', '2026-04-08')) return;
    if (!await setDate('end', '2032-11-01')) return;
    const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('button');
    const registered = Array.from(buttons).find(button => button.textContent?.trim() === 'Registered');
    expect(registered).toBeDefined();
    registered?.click();
    fixture.detectChanges();

    expect(displayedBrands()).toEqual(['Newer match', 'Older match', 'Higher UUID']);
  });

  it('clears either filter independently and restores all supplied events when both are empty', async () => {
    const records = [
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-09', endDate: '2032-11-02', brand: 'Late end' }),
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-08', endDate: '2032-11-01', brand: 'Both match' }),
      detailVehicle({ uuid: highUuid, startDate: '2026-04-07', endDate: '2032-11-01', brand: 'Early start' }),
    ];
    const original = JSON.stringify(records);
    render(records);
    if (!await setDate('start', '2026-04-08')) return;
    if (!await setDate('end', '2032-11-01')) return;
    expect(displayedBrands()).toEqual(['Both match']);

    await setDate('start', '');
    expect(displayedBrands()).toEqual(['Both match', 'Early start']);
    await setDate('start', '2026-04-08');
    await setDate('end', '');
    expect(displayedBrands()).toEqual(['Late end', 'Both match']);
    await setDate('start', '');
    expect(displayedBrands()).toEqual(['Late end', 'Both match', 'Early start']);
    expect(JSON.stringify(records)).withContext('clearing must recover original events').toBe(original);
  });

  it('shows no stale rows for a valid filter with no matches and recovers after clearing', async () => {
    render([detailVehicle({ startDate: '2026-04-08', brand: 'Original' })]);
    if (!await setDate('start', '2027-01-01')) return;

    expect(displayedBrands()).toEqual([]);
    await setDate('start', '');
    expect(displayedBrands()).toEqual(['Original']);
  });

  it('opens the exact filtered event and retains the date controls and list order', async () => {
    const selected = detailVehicle({ uuid: lowUuid, startDate: '2026-04-09', endDate: '2032-11-01', brand: 'Selected' });
    render([
      detailVehicle({ uuid: lowUuid, startDate: '2026-04-07', endDate: '2032-11-01', brand: 'Excluded' }),
      selected,
      detailVehicle({ uuid: highUuid, startDate: '2026-04-08', endDate: '2032-11-01', brand: 'Other' }),
    ]);
    if (!await setDate('start', '2026-04-08')) return;
    if (!await setDate('end', '2032-11-01')) return;
    expect(displayedBrands()).toEqual(['Selected', 'Other']);
    const setItem = jasmine.createSpy('setItem');
    const popup = { sessionStorage: { setItem }, opener: window } as unknown as Window;
    const openWindow = spyOn(window, 'open').and.returnValue(popup);

    const row = (fixture.nativeElement as HTMLElement).querySelector<HTMLTableRowElement>('tbody tr');
    row?.click();
    fixture.detectChanges();

    expect(openWindow).toHaveBeenCalledTimes(1);
    expect(setItem).toHaveBeenCalledOnceWith('vrls2.selected-event', JSON.stringify(selected));
    expect(displayedBrands()).toEqual(['Selected', 'Other']);
    const dates = (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLInputElement>('input[type="date"]');
    expect(Array.from(dates).map(input => input.value)).toEqual(['2026-04-08', '2032-11-01']);
  });
});
