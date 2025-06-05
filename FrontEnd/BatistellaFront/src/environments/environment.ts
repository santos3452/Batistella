import { Environment } from './environment.model';

export const environment: Environment = {
    production: true,
    apiUrl: 'https://www.batistellaycia.shop/api',
    authUrl: 'https://www.batistellaycia.shop/auth',
    usersUrl: 'https://www.batistellaycia.shop/users',
    productsUrl: 'https://www.batistellaycia.shop/api',
    pedidosUrl: 'https://www.batistellaycia.shop/api/products/pedidos',
    mercadoPagoUrl: 'https://www.batistellaycia.shop/api/payments/pagos/mercadopago/preferencia',
    pagoManualUrl: 'https://www.batistellaycia.shop/api/payments/pagos/manual',
    pagosUrl: 'https://www.batistellaycia.shop/api/payments/pagos',
    notificationsUrl: 'https://www.batistellaycia.shop/api/notifications',
    dashboardUrl: 'https://www.batistellaycia.shop/dashboard'
};

