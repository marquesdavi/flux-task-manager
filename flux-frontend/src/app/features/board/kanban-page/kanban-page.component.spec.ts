import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KanbanPageComponent } from './kanban-page.component';

describe('BoardPageComponent', () => {
  let component: KanbanPageComponent;
  let fixture: ComponentFixture<KanbanPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KanbanPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KanbanPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
