import { detailVehicle } from '../vehicle-detail/vehicle-detail.test-support.spec';
import { createSearchHarness, SearchHarness, showSearchResults } from './vehicle-list.search-test-support.spec';

describe('Issue #7: list bindings and audit handoff', () => {
  let h: SearchHarness;
  beforeEach(async () => { h = await createSearchHarness(); });
  afterEach(() => h.http.verify());

  for (const field of ['uuid', 'type'] as const) {
    it(`renders hostile ${field} text without creating elements or event attributes (R-6.2)`, () => {
      const payload = '<img data-list-injection="yes" src=x onerror=alert(1)>';
      const vehicle = detailVehicle({ [field]: payload });
      if (!showSearchResults(h, [vehicle])) return;
      const row: HTMLElement = h.fixture.nativeElement.querySelector('tbody tr');
      expect(row.querySelector('[data-list-injection]')).toBeNull();
      expect(row.querySelector('[onerror]')).toBeNull();
      expect(row.querySelectorAll('td')[field === 'uuid' ? 0 : 2].textContent?.trim())
        .toBe(field === 'uuid' ? payload.substring(0, 8) : payload);
      expect(row.getAttribute('aria-label')).toBe(`Open details for ${vehicle.uuid}`);
    });
  }

  it('carries the clicked source reference and timestamp into its detached popup', () => {
    const selected = { ...detailVehicle(), audit: {
      sourceFile: 'commercial.csv', sourceRow: 29, ingestedAt: '2026-10-29T12:34:56Z',
    } };
    if (!showSearchResults(h, [selected])) return;
    const setItem = jasmine.createSpy('setItem');
    const popup = { sessionStorage: { setItem }, opener: window } as unknown as Window;
    spyOn(window, 'open').and.returnValue(popup);
    h.fixture.nativeElement.querySelector('tbody tr').click();
    expect(setItem).toHaveBeenCalledOnceWith('vrls2.selected-event', JSON.stringify(selected));
    expect(popup.opener).toBeNull();
    h.http.expectNone(() => true);
  });
});
