import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'formatText',
  standalone: true
})
export class FormatTextPipe implements PipeTransform {
  transform(value: string | undefined | null): string {
    if (!value) return '';
    
    // Reemplazar guiones bajos con espacios
    return value.replace(/_/g, ' ');
  }
} 