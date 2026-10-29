import { ComponentFixture } from '@angular/core/testing';
import { Vehicle } from '../vehicle.model';

// Test data only. The model already contains every CSV field and the derived category.
export function detailVehicle(overrides: Partial<Vehicle> = {}): Vehicle {
  return {
    uuid: '00000000-0000-4000-8000-000000000001',
    status: 'REGISTERED',
    startDate: '2026-04-08',
    endDate: '2032-11-01',
    time: '09:05:07',
    userUuid: '11111111-1111-4111-8111-111111111111',
    brand: 'New Brand, 新',
    type: '5-seat',
    category: 'personal',
    ...overrides,
  };
}

export function visibleText(root: HTMLElement): string {
  return root.innerText.replace(/\s+/g, ' ').trim();
}

export function closeButton(root: HTMLElement): HTMLButtonElement | undefined {
  return Array.from(root.querySelectorAll('button')).find(button =>
    /close/i.test(button.getAttribute('aria-label') ?? button.textContent ?? ''),
  );
}

/** Give the real component an actual CSS viewport, including its compiled styles. */
export async function inViewport<T>(
  fixture: ComponentFixture<T>, width: number, height: number,
  check: (root: HTMLElement, viewport: Window) => void,
): Promise<void> {
  const frame = document.createElement('iframe');
  frame.title = 'Detail component layout test';
  frame.style.cssText = `position: fixed; left: 0; top: 0; width: ${width}px; height: ${height}px; border: 0`;
  try {
    await new Promise<void>(resolve => {
      frame.onload = () => resolve();
      frame.src = 'about:blank';
      document.body.appendChild(frame);
    });
    const viewport = frame.contentWindow!;
    const page = viewport.document;
    document.head.querySelectorAll('style').forEach(style => page.head.appendChild(style.cloneNode(true)));
    page.body.appendChild(fixture.nativeElement);
    fixture.detectChanges();
    await new Promise<void>(resolve => viewport.requestAnimationFrame(() => resolve()));
    expect(viewport.innerWidth).withContext('actual layout viewport width').toBe(width);
    expect(viewport.innerHeight).withContext('actual layout viewport height').toBe(height);
    check(fixture.nativeElement, viewport);
  } finally {
    frame.remove();
  }
}
