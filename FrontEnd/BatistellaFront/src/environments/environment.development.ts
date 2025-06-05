import { Environment } from './environment.model';

export const environment: Environment = {
    production: false,
    apiUrl: 'http://localhost:8080/api',
    authUrl: 'http://localhost:8080/api/auth',
    usersUrl: 'http://localhost:8080/api/users',
    productsUrl: 'http://localhost:8083/api',
    pedidosUrl: 'http://localhost:8083/api/pedidos',
    mercadoPagoUrl: 'https://pagos.batistellaycia.shop/api/pagos/mercadopago/preferencia',
    pagoManualUrl: 'https://pagos.batistellaycia.shop/api/pagos/manual',
    pagosUrl: 'http://localhost:8084/api/pagos',
    notificationsUrl: 'http://localhost:8085/api/notifications',
    dashboardUrl: 'http://localhost:8086/dashboard'
};
