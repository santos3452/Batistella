package Payments.Payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;

@Configuration
public class MercadoPagoConfiguration {

    @Value("${mercadopago.access.token}")
    private String mercadoPagoAccessToken;
    
    @Bean
    public String configureMercadoPago() {
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
        return mercadoPagoAccessToken;
    }
    
    @Bean
    public PreferenceClient preferenceClient() {
        return new PreferenceClient();
    }
    
    @Bean
    public PaymentClient paymentClient() {
        return new PaymentClient();
    }
} 