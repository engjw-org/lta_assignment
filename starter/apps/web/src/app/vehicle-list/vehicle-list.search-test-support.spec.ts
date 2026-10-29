import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Vehicle } from '../vehicle.model';
import { VehicleListComponent } from './vehicle-list.component';

export const SEARCH_URL = 'http://localhost:8080/api/vehicles/search';
export interface SearchHarness {
  fixture: ComponentFixture<VehicleListComponent>;
  http: HttpTestingController;
  startupRequests: TestRequest[];
}

export async function createSearchHarness(): Promise<SearchHarness> {
  await TestBed.configureTestingModule({ imports: [VehicleListComponent],
    providers: [provideHttpClient(), provideHttpClientTesting()],
  }).compileComponents();
  const fixture = TestBed.createComponent(VehicleListComponent);
  const http = TestBed.inject(HttpTestingController);
  fixture.detectChanges();
  // Drain the legacy auto-load so other tests report their own missing feature.
  // A dedicated test asserts that these requests must not exist.
  const startupRequests = http.match(() => true);
  startupRequests.forEach(request => request.flush([]));
  fixture.detectChanges();
  return { fixture, http, startupRequests };
}

export function button(root: HTMLElement, text: string): HTMLButtonElement | undefined {
  return Array.from(root.querySelectorAll('button')).find(item => item.textContent?.trim() === text);
}

export function beginSearch(harness: SearchHarness): TestRequest | undefined {
  const search = button(harness.fixture.nativeElement, 'Search');
  expect(search).withContext('missing Search button').toBeDefined();
  if (!search) return undefined;
  search.click();
  harness.fixture.detectChanges();
  const request = harness.http.expectOne(SEARCH_URL);
  expect(request.request.method).toBe('POST');
  return request;
}

export function showSearchResults(harness: SearchHarness, records: Vehicle[], criteria: object = {},
  totalMatches = records.length): boolean {
  const request = beginSearch(harness);
  if (!request) return false;
  expect(request.request.body).toEqual(criteria);
  request.flush({ records, totalMatches, truncated: totalMatches > records.length });
  harness.fixture.detectChanges();
  return true;
}

export function searchField(root: HTMLElement, label: RegExp): HTMLInputElement | HTMLSelectElement | undefined {
  return Array.from(root.querySelectorAll<HTMLInputElement | HTMLSelectElement>('input, select')).find(control => {
    const name = control.getAttribute('aria-label') ??
      Array.from(control.labels ?? []).map(item => item.textContent?.trim()).join(' ');
    return label.test(name);
  });
}

export function enterField(harness: SearchHarness, label: RegExp, value: string): boolean {
  const control = searchField(harness.fixture.nativeElement, label);
  expect(control).withContext(`missing labelled search field ${label}`).toBeDefined();
  if (!control) return false;
  control.value = value;
  control.dispatchEvent(new Event('input', { bubbles: true }));
  control.dispatchEvent(new Event('change', { bubbles: true }));
  harness.fixture.detectChanges();
  return true;
}

export function displayedBrands(harness: SearchHarness): string[] {
  const root = harness.fixture.nativeElement as HTMLElement;
  return Array.from(root.querySelectorAll('tbody tr')).map(row => row.querySelectorAll('td')[1]?.textContent?.trim() ?? '');
}
