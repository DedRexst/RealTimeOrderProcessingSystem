import {Pipe, PipeTransform} from '@angular/core';
import {ProductRef} from '../../services/models/catalog-models';

@Pipe({name: 'productName'})
export class ProductNamePipe implements PipeTransform {
  transform(id: number | string, products: Map<number, ProductRef> | null | undefined): string {
    const p = products?.get(Number(id));
    return p ? p.name : `Product #${id}`;
  }
}
