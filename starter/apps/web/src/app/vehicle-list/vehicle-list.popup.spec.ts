import { ComponentFixture } from '@angular/core/testing';
import { detailVehicle, visibleText } from '../vehicle-detail/vehicle-detail.test-support.spec';
import { VehicleListComponent } from './vehicle-list.component';
import { button, createSearchHarness, SearchHarness, showSearchResults } from './vehicle-list.search-test-support.spec';

describe('Issue #3: open vehicle details in independent browser popups', () => {
  let fixture: ComponentFixture<VehicleListComponent>;
  let h: SearchHarness;
  let openWindow: jasmine.Spy<typeof window.open>;
  const first = detailVehicle();
  const second = detailVehicle({ uuid: '00000000-0000-4000-8000-000000000002', brand: 'Ford' });

  beforeEach(async () => {
    openWindow = spyOn(window, 'open').and.returnValue(null);
    h = await createSearchHarness();
    fixture = h.fixture;
  });
  afterEach(() => h.http.verify());

  function clickRow(index: number): void {
    const row = fixture.nativeElement.querySelectorAll('tbody tr')[index] as HTMLTableRowElement;
    expect(row).withContext(`visible vehicle row ${index}`).toBeDefined();
    row?.click();
    fixture.detectChanges();
  }

  it('opens the clicked row at currentpath/full-uuid in a popup without navigating the list', () => {
    if (!showSearchResults(h, [first, second])) return;
    const beforeUrl = window.location.href;
    clickRow(1);
    expect(openWindow).toHaveBeenCalledTimes(1);
    if (openWindow.calls.count() !== 1) return;
    const [address, target, features] = openWindow.calls.mostRecent().args;
    const url = new URL(String(address), beforeUrl);
    expect(url.origin).toBe(window.location.origin);
    expect(url.pathname).toBe(`${window.location.pathname.replace(/\/$/, '')}/${second.uuid}`);
    expect(target).toBeTruthy();
    expect(['_self', '_parent', '_top']).not.toContain(target!);
    expect(features ?? '').toMatch(/(?:^|,)\s*popup(?:=(?:1|yes|true))?\s*(?:,|$)/i);
    expect(window.location.href).toBe(beforeUrl);
  });

  it('uses different popup destinations so opening another vehicle does not replace the first', () => {
    if (!showSearchResults(h, [first, second])) return;
    clickRow(0);
    clickRow(1);
    expect(openWindow).toHaveBeenCalledTimes(2);
    if (openWindow.calls.count() !== 2) return;
    const [firstCall, secondCall] = openWindow.calls.allArgs();
    expect(new URL(String(firstCall[0]), window.location.href).pathname).toContain(first.uuid);
    expect(new URL(String(secondCall[0]), window.location.href).pathname).toContain(second.uuid);
    const targets = [firstCall[1], secondCall[1]];
    expect(targets.every(target => !!target && !['_self', '_parent', '_top'].includes(target))).toBeTrue();
    expect(targets.every(target => target === '_blank') || targets[0] !== targets[1])
      .withContext('use new windows or distinct window names for comparison').toBeTrue();
  });

  it('preserves the list page, status filter, records and order when opening a detail', () => {
    const records = Array.from({ length: 102 }, (_, index) => detailVehicle({
      uuid: `00000000-0000-4000-8000-${String(index + 1).padStart(12, '0')}`,
    }));
    if (!showSearchResults(h, records)) return;
    button(fixture.nativeElement, 'Registered')!.click();
    fixture.detectChanges();
    button(fixture.nativeElement, 'Next')!.click();
    fixture.detectChanges();
    button(fixture.nativeElement, 'Next')!.click();
    fixture.detectChanges();
    const tableBefore = visibleText(fixture.nativeElement.querySelector('table'));
    clickRow(0);
    expect(openWindow).withContext('this must exercise the popup path').toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.page).toBe(2);
    expect(fixture.componentInstance.selectedStatuses).toEqual(['REGISTERED']);
    expect(fixture.componentInstance.vehicles).toEqual(records);
    expect(visibleText(fixture.nativeElement.querySelector('table'))).toBe(tableBefore);
    h.http.expectNone(() => true);
  });

  it('keeps the list usable and explains when the browser blocks the popup', () => {
    openWindow.and.returnValue(null);
    if (!showSearchResults(h, [first, second])) return;
    expect(() => clickRow(0)).not.toThrow();
    expect(openWindow).toHaveBeenCalledTimes(1);
    const alert = fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement | null;
    expect(alert).withContext('visible popup-blocked feedback').not.toBeNull();
    expect(alert ? visibleText(alert) : '').toMatch(/pop.?up|blocked/i);
    expect(fixture.componentInstance.vehicles).toEqual([first, second]);
  });

  it('passes the complete clicked event into its own window, detaches the opener and keeps the list unchanged', () => {
    const setItem = jasmine.createSpy('setItem');
    const popup = { sessionStorage: { setItem }, opener: window } as unknown as Window;
    openWindow.and.returnValue(popup);
    if (!showSearchResults(h, [first, second])) return;
    clickRow(1);
    expect(setItem).toHaveBeenCalledOnceWith('vrls2.selected-event', JSON.stringify(second));
    expect(popup.opener).toBeNull();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
    expect(fixture.componentInstance.vehicles).toEqual([first, second]);
  });

  it('closes an incomplete popup if the browser refuses the event handoff', () => {
    const close = jasmine.createSpy('close');
    openWindow.and.returnValue({ sessionStorage: { setItem: () => { throw new Error('storage blocked'); } }, close } as unknown as Window);
    if (!showSearchResults(h, [first, second])) return;
    expect(() => clickRow(0)).not.toThrow();
    expect(close).toHaveBeenCalledTimes(1);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();
  });
});
