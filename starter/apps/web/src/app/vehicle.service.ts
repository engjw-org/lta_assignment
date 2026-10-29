import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Vehicle, VehicleSearchCriteria, VehicleSearchResult } from './vehicle.model';

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private readonly baseUrl = 'http://localhost:8080/api/vehicles';

  constructor(private http: HttpClient) {}

  search(criteria: VehicleSearchCriteria): Observable<VehicleSearchResult> {
    return this.http.post<VehicleSearchResult>(`${this.baseUrl}/search`, criteria);
  }

  list(page: number, uuid?: string): Observable<Vehicle[]> {
    const lookup = uuid === undefined ? '' : `&uuid=${encodeURIComponent(uuid)}`;
    return this.http.get<Vehicle[]>(`${this.baseUrl}?page=${page}${lookup}`);
  }

  getOne(uuid: string): Observable<Vehicle> {
    return this.http.get<Vehicle>(`${this.baseUrl}/${uuid}`);
  }
}
