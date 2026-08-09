import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TripDataService } from '../services/trip-data.service';
import { Trip } from '../models/trip';

@Component({
  selector: 'app-trip-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './trip-card.component.html',
  styleUrls: ['./trip-card.component.css']
})
export class TripCardComponent {

  @Input('trip') trip!: Trip;


  @Output() tripDeleted = new EventEmitter<string>();

  constructor(
    private router: Router,
    private tripService: TripDataService
  ) { }


  editTrip(trip: Trip) {
    localStorage.removeItem('tripCode');
    localStorage.setItem('tripCode', trip.code);
    this.router.navigate(['edit-trip']);
  }


  deleteTrip(trip: Trip) {
    if (!confirm(`Delete trip ${trip.name}? This cannot be undone.`)) {
      return;
    }

    this.tripService.deleteTrip(trip.code).subscribe({
      next: () => {
        this.tripDeleted.emit(trip.code);
      },
      error: (error: any) => {
        console.error('Error deleting trip:', error);
      }
    });
  }
}
