import { Environment } from './environment.model';

export const environment: Environment = {
    production: false,
    apiUrl: 'http://localhost:8081/api',
    authUrl: 'http://localhost:8081/api/auth',
    usersUrl: 'http://localhost:8081/api/users',
    pedidosUrl: 'http://localhost:8083/api/pedidos',
    mercadoPagoUrl: 'https://b349-181-4-8-15.ngrok-free.app/api/pagos/mercadopago/preferencia'
};
