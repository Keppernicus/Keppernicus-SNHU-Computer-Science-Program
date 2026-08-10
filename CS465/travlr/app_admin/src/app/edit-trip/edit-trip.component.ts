import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TripDataService } from '../services/trip-data.service';
import { Trip } from '../models/trip';

@Component({
  selector: 'app-edit-trip',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-trip.component.html',
  styleUrls: ['./edit-trip.component.css']
})
export class EditTripComponent implements OnInit {

  editForm!: FormGroup;
  trip!: Trip;
  submitted = false;
  message = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private tripService: TripDataService
  ) { }

  ngOnInit(): void {
    const tripCode = localStorage.getItem('tripCode');

    if (!tripCode) {
      alert('Something went wrong, no trip code was found.');
      this.router.navigate(['']);
      return;
    }

    this.editForm = this.formBuilder.group({
      _id: [],
      code: [tripCode, Validators.required],
      name: ['', Validators.required],
      length: ['', Validators.required],
      start: ['', Validators.required],
      resort: ['', Validators.required],
      perPerson: ['', Validators.required],
      image: ['', Validators.required],
      description: ['', Validators.required]
    });

    this.tripService.getTrip(tripCode).subscribe({
      next: (value: Trip[]) => {
        if (value.length > 0) {
          // the date input needs yyyy-MM-dd
          const trip = { ...value[0] } as any;
          if (trip.start) {
            const d = new Date(trip.start);
            const pad = (n: number) => n.toString().padStart(2, '0');
            trip.start = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
          }
          this.editForm.patchValue(trip);
          this.message = '';
        } else {
          this.message = `Trip ${tripCode} not found`;
        }
      },
      error: (error: any) => {
        this.message = 'Error loading trip';
        console.error('Error loading trip:', error);
      }
    });
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.editForm.valid) {
      this.tripService.updateTrip(this.editForm.value).subscribe({
        next: () => {
          this.router.navigate(['']);
        },
        error: (error: any) => {
          console.error('Error updating trip:', error);
        }
      });
    }
  }

  get f() { return this.editForm.controls; }
}
