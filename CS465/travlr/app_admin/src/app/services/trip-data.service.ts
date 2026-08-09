import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Trip } from '../models/trip';

@Injectable({
  providedIn: 'root'
})
export class TripDataService {

  // express API
  private apiBaseUrl = 'http://localhost:3000/api';
  private tripUrl = `${this.apiBaseUrl}/trips`;

  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) { }

  // GET /api/trips
  getTrips(): Observable<Trip[]> {
    return this.http.get<Trip[]>(this.tripUrl);
  }

  // GET /api/trips/:tripCode
  getTrip(tripCode: string): Observable<Trip[]> {
    return this.http.get<Trip[]>(`${this.tripUrl}/${tripCode}`);
  }

  // POST /api/trips
  addTrip(formData: Trip): Observable<Trip> {
    return this.http.post<Trip>(this.tripUrl, formData, this.httpOptions);
  }

  // PUT /api/trips/:tripCode
  updateTrip(formData: Trip): Observable<Trip> {
    return this.http.put<Trip>(
      `${this.tripUrl}/${formData.code}`,
      formData,
      this.httpOptions
    );
  }

  // DELETE /api/trips/:tripCode
  deleteTrip(tripCode: string): Observable<any> {
    return this.http.delete(`${this.tripUrl}/${tripCode}`, this.httpOptions);
  }
}
