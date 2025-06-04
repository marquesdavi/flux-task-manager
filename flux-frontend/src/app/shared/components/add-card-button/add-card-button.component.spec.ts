import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddCardButtonComponent } from './add-card-button.component';

describe('AddCardButtonComponent', () => {
  let component: AddCardButtonComponent;
  let fixture: ComponentFixture<AddCardButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCardButtonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddCardButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
