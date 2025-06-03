import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BoardManagementPageComponent } from './board-management-page.component';

describe('BoardManagementPageComponent', () => {
  let component: BoardManagementPageComponent;
  let fixture: ComponentFixture<BoardManagementPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BoardManagementPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BoardManagementPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
