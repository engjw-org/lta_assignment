import { detailVehicle, inViewport, visibleText } from '../vehicle-detail/vehicle-detail.test-support.spec';
import { Vehicle } from '../vehicle.model';
import { beginSearch, button, createSearchHarness, displayedBrands, enterField, searchField,
  SearchHarness, showSearchResults } from './vehicle-list.search-test-support.spec';

// Supersedes live date filtering. Global matching/order/cap move to VehicleSearchTest.
describe('Issue #5: submitted search, capped results and local status filters', () => {
  let h: SearchHarness;
  const query = /uuid.*brand/i;
  const type = /(?:seat|vehicle) type/i;
  const start = /^start date/i;
  const end = /^end date/i;

  beforeEach(async () => { h = await createSearchHarness(); });
  afterEach(() => h.http.verify());

  function records(count: number): Vehicle[] {
    return Array.from({ length: count }, (_, index) => detailVehicle({
      uuid: `00000000-0000-4000-8000-${String(index + 1).padStart(12, '0')}`,
      brand: `Vehicle ${index + 1}`,
    }));
  }

  function click(text: string): void {
    const target = button(h.fixture.nativeElement, text);
    expect(target).withContext(`missing ${text} button`).toBeDefined();
    target?.click();
    h.fixture.detectChanges();
  }

  it('waits for Search before fetching or showing records', () => {
    expect(h.startupRequests.length).withContext('no automatic list load').toBe(0);
    expect(displayedBrands(h)).toEqual([]);
    expect(visibleText(h.fixture.nativeElement)).toMatch(/search/i);
    h.http.expectNone(() => true);
  });

  it('places optional fields on row one, Search on row two, and status filters below at 1280x720', async () => {
    const root = h.fixture.nativeElement as HTMLElement;
    const fields = [query, start, end, type].map(label => searchField(root, label));
    fields.forEach((field, index) => expect(field).withContext(`field ${index}`).toBeDefined());
    const search = button(root, 'Search');
    expect(search).toBeDefined();
    if (fields.some(field => !field) || !search) return;
    fields.forEach(field => expect(field!.required).withContext('all fields optional').toBeFalse());
    expect(searchField(root, start)!.type).toBe('date');
    expect(searchField(root, end)!.type).toBe('date');
    await inViewport(h.fixture, 1280, 720, (mounted, viewport) => {
      const bounds = fields.map(field => field!.getBoundingClientRect());
      const searchBounds = search.getBoundingClientRect();
      const statuses = ['Registered', 'Deregistered'].map(text => button(mounted, text)!);
      expect(Math.max(...bounds.map(box => box.top)) - Math.min(...bounds.map(box => box.top)))
        .withContext('search inputs share row one').toBeLessThanOrEqual(4);
      expect(searchBounds.top).toBeGreaterThanOrEqual(Math.max(...bounds.map(box => box.bottom)));
      statuses.forEach(status => expect(status.getBoundingClientRect().top).toBeGreaterThanOrEqual(searchBounds.bottom));
      expect(viewport.document.documentElement.scrollWidth).toBeLessThanOrEqual(1280);
      expect(statuses[0].getBoundingClientRect().bottom).toBeLessThanOrEqual(720);
    });
  });

  it('submits all four optional fields in one JSON request only after Search is clicked', () => {
    if (!enterField(h, query, 'Toyota')) return;
    if (!enterField(h, type, '5-seat')) return;
    if (!enterField(h, start, '2026-04-08')) return;
    if (!enterField(h, end, '2032-11-01')) return;
    h.http.expectNone(() => true);
    expect(displayedBrands(h)).toEqual([]);
    const expected = detailVehicle();
    if (!showSearchResults(h, [expected], { query: 'Toyota', type: '5-seat', startDate: '2026-04-08', endDate: '2032-11-01' })) return;
    expect(displayedBrands(h)).toEqual([expected.brand]);
  });

  for (const [field, key, value] of [
    [start, 'startDate', '2026-04-08'], [end, 'endDate', '2032-11-01'],
    [query, 'query', '00000000-'], [type, 'type', '5-seat'],
  ] as const) {
    it(`allows ${key} to be the only supplied criterion`, () => {
      if (!enterField(h, field, value)) return;
      if (!showSearchResults(h, [detailVehicle()], { [key]: value })) return;
      expect(displayedBrands(h)).toEqual([detailVehicle().brand]);
    });
  }

  it('blank Search pages results locally in groups of 50 without dropping a row', () => {
    if (!showSearchResults(h, records(61))) return;
    expect(displayedBrands(h)).toEqual(records(50).map(record => record.brand));
    expect(button(h.fixture.nativeElement, 'Previous')?.disabled).toBeTrue();
    click('Next');
    expect(displayedBrands(h)).toEqual(records(61).slice(50).map(record => record.brand));
    expect(button(h.fixture.nativeElement, 'Next')?.disabled).toBeTrue();
    click('Previous');
    expect(displayedBrands(h).length).toBe(50);
    h.http.expectNone(() => true);
  });

  it('draft edits leave results unchanged until Search and a new search resets the page', () => {
    if (!showSearchResults(h, records(61))) return;
    click('Next');
    const before = displayedBrands(h);
    if (!enterField(h, start, '2035-01-01')) return;
    expect(displayedBrands(h)).toEqual(before);
    h.http.expectNone(() => true);
    if (!showSearchResults(h, [detailVehicle({ startDate: '2036-01-01', brand: 'New result' })], { startDate: '2035-01-01' })) return;
    expect(h.fixture.componentInstance.page).toBe(0);
    expect(displayedBrands(h)).toEqual(['New result']);
  });

  it('clears criteria only on resubmission and replaces the previous result set', () => {
    if (!enterField(h, start, '2026-04-08')) return;
    if (!enterField(h, end, '2032-11-01')) return;
    if (!showSearchResults(h, [detailVehicle({ brand: 'Restricted' })], { startDate: '2026-04-08', endDate: '2032-11-01' })) return;
    enterField(h, start, '');
    expect(displayedBrands(h)).toEqual(['Restricted']);
    if (!showSearchResults(h, [detailVehicle({ brand: 'End only' })], { endDate: '2032-11-01' })) return;
    enterField(h, end, '');
    expect(displayedBrands(h)).toEqual(['End only']);
    if (!showSearchResults(h, records(2))) return;
    expect(displayedBrands(h)).toEqual(['Vehicle 1', 'Vehicle 2']);
  });

  it('filters statuses across returned records before paging, with either or both statuses allowed', () => {
    const results = records(61).map((record, index) => ({ ...record,
      status: index < 50 ? 'REGISTERED' as const : 'DEREGISTERED' as const }));
    if (!showSearchResults(h, results)) return;
    click('Next');
    click('Deregistered');
    expect(h.fixture.componentInstance.page).toBe(0);
    expect(displayedBrands(h)).toEqual(results.slice(50).map(record => record.brand));
    click('Registered');
    expect(displayedBrands(h)).toEqual(results.slice(0, 50).map(record => record.brand));
    click('Deregistered');
    expect(displayedBrands(h)).toEqual(results.slice(0, 50).map(record => record.brand));
    click('Registered');
    click('Next');
    expect(displayedBrands(h)).toEqual(results.slice(50).map(record => record.brand));
    h.http.expectNone(() => true);
  });

  it('shows the 2,000 cap, total matches and refine notice, clearing the notice after a narrower search', () => {
    if (!showSearchResults(h, records(2000), {}, 2501)) return;
    const text = visibleText(h.fixture.nativeElement);
    expect(text).toMatch(/2,?000/);
    expect(text).toMatch(/2,?501/);
    expect(text).toMatch(/refine|narrow/i);
    expect(displayedBrands(h).length).toBe(50);
    if (!enterField(h, query, 'Toyota')) return;
    if (!showSearchResults(h, [detailVehicle()], { query: 'Toyota' })) return;
    expect(visibleText(h.fixture.nativeElement)).not.toMatch(/refine|narrow/i);
  });

  it('prevents duplicate submissions while the search response is pending', () => {
    const request = beginSearch(h);
    if (!request) return;
    const search = button(h.fixture.nativeElement, 'Search')!;
    expect(search.disabled).toBeTrue();
    search.click();
    h.http.expectNone(() => true);
    request.flush({ records: [], totalMatches: 0, truncated: false });
    h.fixture.detectChanges();
    expect(button(h.fixture.nativeElement, 'Search')?.disabled).toBeFalse();
  });

  it('replaces stale rows with an empty-state message and can search again', () => {
    if (!showSearchResults(h, [detailVehicle()])) return;
    if (!showSearchResults(h, [])) return;
    expect(displayedBrands(h)).toEqual([]);
    expect(visibleText(h.fixture.nativeElement)).toMatch(/no (?:matching )?(?:records|results)/i);
    if (!showSearchResults(h, records(1))) return;
    expect(displayedBrands(h)).toEqual(['Vehicle 1']);
  });

  it('reports a failed search without backend details and permits retry', () => {
    const request = beginSearch(h);
    if (!request) return;
    request.flush({ message: 'Exception at /server/private/config' }, { status: 500, statusText: 'Server Error' });
    h.fixture.detectChanges();
    const alert = h.fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement | null;
    expect(alert).toBeTruthy();
    expect(alert ? visibleText(alert) : '').toMatch(/search|retry|try again/i);
    expect(visibleText(h.fixture.nativeElement)).not.toMatch(/Exception|\/server\/private/);
    expect(button(h.fixture.nativeElement, 'Search')?.disabled).toBeFalse();
    if (!showSearchResults(h, records(1))) return;
    expect(h.fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });

  it('renders result text safely and opens the exact event without changing search state', () => {
    const selected = detailVehicle({ brand: '<img src=x onerror=alert(1)>' });
    if (!enterField(h, start, '2026-04-08')) return;
    if (!enterField(h, end, '2032-11-01')) return;
    if (!showSearchResults(h, [selected], { startDate: '2026-04-08', endDate: '2032-11-01' })) return;
    expect(displayedBrands(h)).toEqual([selected.brand]);
    expect(h.fixture.nativeElement.querySelector('tbody img')).toBeNull();
    const setItem = jasmine.createSpy('setItem');
    const popup = { sessionStorage: { setItem }, opener: window } as unknown as Window;
    const openWindow = spyOn(window, 'open').and.returnValue(popup);
    h.fixture.nativeElement.querySelector('tbody tr').click();
    h.fixture.detectChanges();
    expect(openWindow).toHaveBeenCalledTimes(1);
    expect(setItem).toHaveBeenCalledOnceWith('vrls2.selected-event', JSON.stringify(selected));
    expect(searchField(h.fixture.nativeElement, start)?.value).toBe('2026-04-08');
    expect(searchField(h.fixture.nativeElement, end)?.value).toBe('2032-11-01');
    expect(displayedBrands(h)).toEqual([selected.brand]);
    h.http.expectNone(() => true);
  });
});
