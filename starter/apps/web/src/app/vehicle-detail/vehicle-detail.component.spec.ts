import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VehicleDetailComponent } from './vehicle-detail.component';
import { closeButton, detailVehicle, inViewport, visibleText } from './vehicle-detail.test-support.spec';

describe('Issue #3: reusable vehicle details (R-1.6, R-2.3, R-2.4, R-6.2)', () => {
  let fixture: ComponentFixture<VehicleDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [VehicleDetailComponent] }).compileComponents();
    fixture = TestBed.createComponent(VehicleDetailComponent);
  });

  function render(vehicle = detailVehicle()): HTMLElement {
    fixture.componentRef.setInput('vehicle', vehicle);
    fixture.detectChanges();
    return fixture.nativeElement;
  }

  it('shows labelled values for all eight CSV fields and category, including full UUIDs', () => {
    const vehicle = detailVehicle();
    const text = visibleText(render(vehicle));
    for (const label of ['UUID', 'Status', 'Start date', 'End date', 'Time', 'User UUID', 'Brand', 'Type', 'Category']) {
      expect(text.toLowerCase()).withContext(`missing field label: ${label}`).toContain(label.toLowerCase());
    }
    for (const value of [vehicle.uuid, vehicle.status, '08 Apr 2026', '01 Nov 2032',
      vehicle.time!, vehicle.userUuid, vehicle.brand, vehicle.type, vehicle.category]) {
      expect(text.toLowerCase()).withContext(`missing field value: ${value}`).toContain(value.toLowerCase());
    }
  });

  it('accepts the complete record after initial rendering without mutating the input', () => {
    expect(() => fixture.detectChanges()).not.toThrow();
    const vehicle = Object.freeze(detailVehicle());
    const before = { ...vehicle };
    const text = visibleText(render(vehicle));
    expect(text).toContain(vehicle.userUuid);
    expect(text).toContain(vehicle.brand);
    expect(text).toContain('08 Apr 2026');
    expect(vehicle).toEqual(before);
  });

  it('shows the selected event when another event with the same vehicle UUID is passed in', () => {
    const registered = detailVehicle();
    render(registered);
    const deregistered = detailVehicle({ status: 'DEREGISTERED', startDate: '2026-05-09',
      time: '18:45:06', userUuid: '22222222-2222-4222-8222-222222222222' });
    const text = visibleText(render(deregistered));
    expect(text).toContain(deregistered.uuid);
    expect(text).toContain('DEREGISTERED');
    expect(text).toContain('09 May 2026');
    expect(text).toContain('18:45:06');
    expect(text).toContain(deregistered.userUuid);
    expect(text).not.toContain('08 Apr 2026');
    expect(text).not.toContain(registered.userUuid);
  });

  it('clears the previous record safely when the input becomes null', () => {
    const vehicle = detailVehicle();
    render(vehicle);
    expect(visibleText(fixture.nativeElement)).withContext('record was visible before clearing').toContain(vehicle.uuid);
    fixture.componentRef.setInput('vehicle', null);
    expect(() => fixture.detectChanges()).not.toThrow();
    expect(visibleText(fixture.nativeElement)).not.toContain(vehicle.uuid);
  });

  for (const [date, formatted] of [
    ['2026-04-08', '08 Apr 2026'],
    ['2022-11-01', '01 Nov 2022'],
    ['2024-02-29', '29 Feb 2024'],
  ]) {
    it(`renders both ${date} date fields as ${formatted}`, () => {
      const vehicle = detailVehicle({ startDate: date, endDate: date });
      const text = visibleText(render(vehicle));
      expect(text.split(formatted).length - 1).withContext('both start and end dates').toBe(2);
      expect(text).not.toContain(date);
      expect(vehicle.startDate).toBe(date);
      expect(vehicle.endDate).toBe(date);
    });
  }

  for (const field of ['uuid', 'brand', 'type', 'userUuid'] as const) {
    it(`renders markup in ${field} literally instead of creating elements (R-6.2)`, () => {
      const payload = `<b data-detail-injection="${field}">X</b>`;
      const root = render(detailVehicle({ [field]: payload }));
      expect(visibleText(root)).withContext('the untrusted value must still be visible').toContain(payload);
      expect(root.querySelector('[data-detail-injection]')).withContext('no injected HTML element').toBeNull();
    });
  }

  it('emits close from a visible Close control without changing the record', () => {
    const vehicle = detailVehicle();
    const root = render(vehicle);
    const onClose = jasmine.createSpy('close');
    fixture.componentInstance.close.subscribe(onClose);
    const button = closeButton(root);
    expect(button).withContext('a labelled Close button must exist').toBeDefined();
    button?.click();
    expect(onClose).toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.vehicle).toEqual(vehicle);
  });

  it('keeps two detail instances independent while comparing different vehicles', () => {
    const first = detailVehicle();
    const second = detailVehicle({ uuid: '00000000-0000-4000-8000-000000000002', brand: 'Ford' });
    render(first);
    const other = TestBed.createComponent(VehicleDetailComponent);
    try {
      other.componentRef.setInput('vehicle', second);
      other.detectChanges();
      expect(visibleText(fixture.nativeElement)).toContain(first.uuid);
      expect(visibleText(fixture.nativeElement)).not.toContain(second.uuid);
      expect(visibleText(other.nativeElement)).toContain(second.uuid);
      expect(visibleText(other.nativeElement)).not.toContain(first.uuid);
      const firstClosed = jasmine.createSpy('firstClosed');
      fixture.componentInstance.close.subscribe(firstClosed);
      closeButton(other.nativeElement)?.click();
      expect(firstClosed).not.toHaveBeenCalled();
      expect(visibleText(fixture.nativeElement)).toContain(first.uuid);
    } finally {
      other.destroy();
    }
  });

  for (const [width, height] of [[1280, 720], [360, 640]]) {
    it(`lays out the complete record in a responsive grid at ${width}x${height}`, async () => {
      const vehicle = detailVehicle({ brand: 'B'.repeat(100), type: 'T'.repeat(50) });
      render(vehicle);
      await inViewport(fixture, width, height, (root, viewport) => {
        expect(visibleText(root)).toContain(vehicle.uuid);
        expect(visibleText(root)).toContain(vehicle.brand);
        expect(visibleText(root)).toContain(vehicle.type);
        for (const value of [vehicle.uuid, vehicle.userUuid, vehicle.brand, vehicle.type,
          vehicle.status, vehicle.category, vehicle.time!, '08 Apr 2026', '01 Nov 2032']) {
          const walker = viewport.document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
          let textNode: Node | null;
          let found = false;
          while ((textNode = walker.nextNode())) {
            if (!(textNode.textContent ?? '').toLowerCase().includes(value.toLowerCase())) continue;
            const range = viewport.document.createRange();
            range.selectNodeContents(textNode);
            const bounds = range.getBoundingClientRect();
            expect(bounds.width).withContext(`visible ${value}`).toBeGreaterThan(0);
            expect(bounds.height).withContext(`visible ${value}`).toBeGreaterThan(0);
            expect(bounds.left).toBeGreaterThanOrEqual(0);
            expect(bounds.right).toBeLessThanOrEqual(width);
            if (width === 1280) {
              expect(bounds.top).toBeGreaterThanOrEqual(0);
              expect(bounds.bottom).withContext('all desktop values fit vertically').toBeLessThanOrEqual(height);
            }
            found = true;
            break;
          }
          expect(found).withContext(`rendered text node for ${value}`).toBeTrue();
        }
        const grid = [root, ...Array.from(root.querySelectorAll<HTMLElement>('*'))].find(element =>
          viewport.getComputedStyle(element).display === 'grid'
          && element.textContent?.includes(vehicle.userUuid),
        );
        expect(grid).withContext('the detail fields must have a grid layout').toBeDefined();
        if (grid) {
          const columns = viewport.getComputedStyle(grid).gridTemplateColumns.split(' ').filter(Boolean);
          if (width === 1280) {
            expect(columns.length).withContext('desktop fields use more than one column').toBeGreaterThan(1);
          } else {
            expect(columns.length).withContext('narrow detail window uses one column').toBe(1);
          }
          expect(grid.getBoundingClientRect().width).toBeGreaterThan(0);
        }
        expect(viewport.document.documentElement.scrollWidth).withContext('no horizontal overflow').toBeLessThanOrEqual(width);
        const button = closeButton(root);
        expect(button).withContext('Close remains available').toBeDefined();
        if (width === 1280 && button) {
          const bounds = button.getBoundingClientRect();
          expect(bounds.height).toBeGreaterThan(0);
          expect(bounds.top).toBeGreaterThanOrEqual(0);
          expect(bounds.bottom).toBeLessThanOrEqual(height);
        }
      });
    });
  }
});
