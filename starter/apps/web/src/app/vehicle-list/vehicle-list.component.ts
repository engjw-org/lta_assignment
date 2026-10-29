import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VehicleService } from '../vehicle.service';
import { Vehicle, VehicleSearchCriteria } from '../vehicle.model';
import { SELECTED_EVENT_KEY } from '../vehicle-detail/vehicle-detail-state';

@Component({
  selector: 'app-vehicle-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './vehicle-list.component.html',
  styleUrls: ['./vehicle-list.component.css'],
})
export class VehicleListComponent {
  readonly pageSize = 50;
  vehicles: Vehicle[] = [];
  selectedStatuses: string[] = [];
  searchQuery = '';
  startDate = '';
  endDate = '';
  seatType = '';
  page = 0;
  totalMatches = 0;
  truncated = false;
  hasSearched = false;
  loading = false;
  searchError = '';
  popupMessage = '';

  constructor(private vehicleService: VehicleService) {}

  search(): void {
    if (this.loading) return;
    const criteria: VehicleSearchCriteria = {};
    if (this.searchQuery.trim()) criteria.query = this.searchQuery.trim();
    if (this.startDate) criteria.startDate = this.startDate;
    if (this.endDate) criteria.endDate = this.endDate;
    if (this.seatType.trim()) criteria.type = this.seatType.trim();

    this.loading = true;
    this.searchError = '';
    this.popupMessage = '';
    this.vehicleService.search(criteria).subscribe({
      next: result => {
        this.vehicles = result.records;
        this.totalMatches = result.totalMatches;
        this.truncated = result.truncated;
        this.hasSearched = true;
        this.page = 0;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.searchError = 'Unable to search vehicles. Please try again.';
      },
    });
  }

  toggleStatus(status: string): void {
    const idx = this.selectedStatuses.indexOf(status);
    if (idx >= 0) {
      this.selectedStatuses.splice(idx, 1);
    } else {
      this.selectedStatuses.push(status);
    }
    this.page = 0;
  }

  filteredVehicles(): Vehicle[] {
    // The API supplies ordered results; status changes only filter that returned set.
    return this.vehicles.filter((v) =>
      this.selectedStatuses.length === 0 || this.selectedStatuses.includes(v.status),
    );
  }

  visibleVehicles(): Vehicle[] {
    const start = this.page * this.pageSize;
    return this.filteredVehicles().slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredVehicles().length / this.pageSize));
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) this.page += 1;
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page -= 1;
    }
  }

  open(vehicle: Vehicle): void {
    this.popupMessage = '';
    const path = `${window.location.pathname.replace(/\/$/, '')}/${encodeURIComponent(vehicle.uuid)}`;
    const popup = window.open(path, '_blank', 'popup,width=1100,height=640,resizable=yes,scrollbars=yes');
    if (!popup) {
      this.popupMessage = 'The popup was blocked. Allow popups for this site, then open the vehicle again.';
      return;
    }
    try {
      // The initial same-origin window is available before its navigation completes.
      popup.sessionStorage.setItem(SELECTED_EVENT_KEY, JSON.stringify(vehicle));
      popup.opener = null;
    } catch {
      popup.close();
      this.popupMessage = 'Unable to pass the selected event to the popup. Allow browser storage and try again.';
    }
  }
}
