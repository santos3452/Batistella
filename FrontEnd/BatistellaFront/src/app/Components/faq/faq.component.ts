import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './faq.component.html',
  styleUrl: './faq.component.css'
})
export class FaqComponent {
  openItems: boolean[] = [false, false, false, false, false, false];

  toggleFaq(index: number): void {
    this.openItems[index] = !this.openItems[index];
  }
}
