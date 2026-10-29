import { Vehicle } from '../vehicle.model';
import { createSearchHarness, SearchHarness, showSearchResults } from './vehicle-list.search-test-support.spec';

describe('Issue #1: list start-date display (R-2.3)', () => {
  let h: SearchHarness;

  beforeEach(async () => {
    h = await createSearchHarness();
  });
  afterEach(() => h.http.verify());

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
      if (!showSearchResults(h, [vehicle])) return;

      const table: HTMLTableElement = h.fixture.nativeElement.querySelector('table');
      const dateColumn = Array.from(table.querySelectorAll('thead th'))
        .findIndex((heading) => heading.textContent?.trim() === 'Start Date');
      expect(dateColumn).toBeGreaterThanOrEqual(0);
      const cell = table.querySelectorAll('tbody tr')[0]?.querySelectorAll('td')[dateColumn];
      expect(cell?.textContent?.trim()).toBe(expected);
      expect(vehicle.startDate).toBe(input);
    });
  }
});
