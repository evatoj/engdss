import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaTransacao } from './consulta-transacao';

describe('ConsultaTransacao', () => {
  let component: ConsultaTransacao;
  let fixture: ComponentFixture<ConsultaTransacao>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsultaTransacao]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaTransacao);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
