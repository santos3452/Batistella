import { Environment } from './environment.model';

export const environment: Environment = {
    production: false,
    apiUrl: 'http://localhost:8081/api',
    authUrl: 'http://localhost:8081/api/auth',
    usersUrl: 'http://localhost:8081/api/users',
    pedidosUrl: 'http://localhost:8083/api/pedidos',
    mercadoPagoUrl: 'https://visiting-ringtones-museums-decent.trycloudflare.com/api/pagos/mercadopago/preferencia',
    pagoManualUrl: 'https://visiting-ringtones-museums-decent.trycloudflare.com/api/pagos/manual',
    pagosUrl: 'http://localhost:8080/api/pagos'
};
