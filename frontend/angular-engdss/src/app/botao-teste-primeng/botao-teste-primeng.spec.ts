import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BotaoTestePrimeng } from './botao-teste-primeng';

describe('BotaoTestePrimeng', () => {
  let component: BotaoTestePrimeng;
  let fixture: ComponentFixture<BotaoTestePrimeng>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BotaoTestePrimeng]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BotaoTestePrimeng);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
