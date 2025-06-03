import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MaterialSymbolComponent } from './material-symbol.component';

describe('MaterialSymbolComponent', () => {
  let component: MaterialSymbolComponent;
  let fixture: ComponentFixture<MaterialSymbolComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaterialSymbolComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaterialSymbolComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
