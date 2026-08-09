import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TripCardComponent } from '../trip-card/trip-card.component';
import { TripDataService } from '../services/trip-data.service';
import { Trip } from '../models/trip';

@Component({
  selector: 'app-trip-listing',
  standalone: true,
  imports: [CommonModule, TripCardComponent],
  templateUrl: './trip-listing.component.html',
  styleUrls: ['./trip-listing.component.css']
})
export class TripListingComponent implements OnInit {

  trips: Trip[] = [];
  message: string = '';

  constructor(
    private tripDataService: TripDataService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.getStuff();
  }

  addTrip(): void {
    this.router.navigate(['add-trip']);
  }


  onTripDeleted(): void {
    this.getStuff();
  }

  private getStuff(): void {
    this.tripDataService.getTrips().subscribe({
      next: (value: Trip[]) => {
        this.trips = value;
        this.message = value.length > 0
          ? ''
          : 'No trips retrieved from database';
      },
      error: (error: any) => {
        this.trips = [];
        this.message = error.status === 404
          ? 'No trips retrieved from database'
          : 'Error retrieving trips';
        console.error('Error retrieving trips:', error);
      }
    });
  }
}
