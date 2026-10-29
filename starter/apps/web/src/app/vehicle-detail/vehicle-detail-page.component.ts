import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';
import { Vehicle } from '../vehicle.model';
import { VehicleService } from '../vehicle.service';
import { VehicleDetailComponent } from './vehicle-detail.component';
import { SELECTED_EVENT_KEY } from './vehicle-detail-state';

@Component({
  selector: 'app-vehicle-detail-page',
  standalone: true,
  imports: [CommonModule, VehicleDetailComponent],
  template: `
    <p *ngIf="loading" role="status">Loading vehicle details…</p>
    <p *ngIf="message" role="alert">{{ message }}</p>
    <div class="chooser" *ngIf="events.length > 1">
      <label for="event-choice">Choose a status event</label>
      <select id="event-choice" [value]="selectedIndex" (change)="selectEvent($any($event.target).value)">
        <option value="" disabled>Select an event</option>
        <option *ngFor="let event of events; index as index" [value]="index">
          {{ index + 1 }}. {{ event.status }} · {{ event.startDate | date: 'dd MMM yyyy' }} · {{ event.time }} · {{ event.category }}
        </option>
      </select>
    </div>
    <app-vehicle-detail *ngIf="vehicle" [vehicle]="vehicle" (close)="closeWindow()"></app-vehicle-detail>
  `,
  styles: [`
    :host { display: block; max-width: 1100px; margin: 0 auto; }
    .chooser { display: grid; gap: 8px; margin: 8px 0 16px; }
    select { font: inherit; padding: 8px; width: 100%; min-width: 0; }
  `],
})
export class VehicleDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(VehicleService);
  private readonly destroyRef = inject(DestroyRef);
  events: Vehicle[] = [];
  vehicle: Vehicle | null = null;
  selectedIndex = '';
  loading = true;
  message = '';

  ngOnInit(): void {
    this.route.paramMap.pipe(
      switchMap(params => {
        this.events = [];
        this.vehicle = null;
        this.selectedIndex = '';
        this.message = '';
        this.loading = true;
        const uuid = params.get('uuid') ?? '';
        if (!/^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$/i.test(uuid)) {
          this.message = 'Invalid vehicle UUID. Open a vehicle from the list.';
          return of([]);
        }
        return this.service.list(0, uuid).pipe(catchError(() => {
          this.message = 'Unable to load vehicle details. Please try again.';
          return of([]);
        }));
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(events => {
      this.loading = false;
      this.events = events;
      if (this.message) return;
      if (!events.length) {
        this.message = 'Vehicle not found. Return to the list to choose a vehicle.';
        return;
      }
      const cachedIndex = this.cachedEventIndex(events);
      if (cachedIndex >= 0) this.selectEvent(String(cachedIndex));
      else if (events.length === 1) this.selectEvent('0');
    });
  }

  selectEvent(index: string): void {
    const event = index === '' ? undefined : this.events[Number(index)];
    if (!event) return;
    this.selectedIndex = index;
    this.vehicle = event;
    try {
      sessionStorage.setItem(SELECTED_EVENT_KEY, JSON.stringify(event));
    } catch {
      this.message = 'This browser cannot save the selection. Choose the event again after refreshing.';
    }
  }

  closeWindow(): void {
    window.close();
  }

  private cachedEventIndex(events: Vehicle[]): number {
    try {
      const cached: unknown = JSON.parse(sessionStorage.getItem(SELECTED_EVENT_KEY) ?? 'null');
      if (!cached || typeof cached !== 'object') return -1;
      // Storage is only a selection hint. Render the validated API record, never stored fields.
      return events.findIndex(event => (Object.keys(event) as (keyof Vehicle)[])
        .every(key => event[key] === (cached as Partial<Vehicle>)[key]));
    } catch {
      return -1;
    }
  }
}
