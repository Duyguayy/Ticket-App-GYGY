# Ticket App

Ticket App, etkinlik bileti satın alma, biletleri görüntüleme ve kapıda QR kod ile check-in yapılmasını sağlayan bir Android uygulamasıdır. Uygulama Kotlin ve Jetpack Compose ile geliştirilmiş olup, Express.js ve PostgreSQL üzerinde çalışan bir REST API'yi tüketir.

## İçindekiler

- [Genel Bakış](#genel-bakış)
- [Roller ve Yetkiler](#roller-ve-yetkiler)
- [Özellikler](#özellikler)
- [Mimari](#mimari)
- [Proje Yapısı](#proje-yapısı)
- [Kullanılan Teknolojiler](#kullanılan-teknolojiler)
- [Gereksinimler](#gereksinimler)
- [Kurulum](#kurulum)
- [Yapılandırma](#yapılandırma)
- [Derleme ve Çalıştırma](#derleme-ve-çalıştırma)
- [API Referansı](#api-referansı)
- [Güvenlik](#güvenlik)
- [Bilinen Sınırlamalar](#bilinen-sınırlamalar)

## Genel Bakış

Uygulama, üç farklı kullanıcı rolüne hizmet verecek şekilde tasarlanmıştır: bilet satın alan son kullanıcılar, kapıda bilet kontrolü yapan görevliler ve etkinlik/bilet yönetimini üstlenen yöneticiler. İstemci tarafı, sunucu tarafından sağlanan REST API ile HTTPS/JSON üzerinden haberleşir ve kimlik doğrulamasında JWT tabanlı erişim/yenileme (access/refresh) token akışı kullanılır.

## Roller ve Yetkiler

| Rol | Açıklama |
| --- | --- |
| USER | Etkinlikleri görüntüler, bilet satın alır ve kendi biletlerini görür. |
| STAFF | Yalnızca kendisine atanmış etkinliklerin biletlerini QR kod ile tarayıp giriş onayı verir. |
| ADMIN | Etkinlik ve bilet türü yönetimi, personel atama ve satış raporlarına erişim. STAFF yetkilerini de kapsar. |

## Özellikler

- E-posta ve şifre ile kayıt olma ve giriş yapma
- Yaklaşan etkinliklerin listelenmesi ve etkinlik detaylarının görüntülenmesi
- Bilet türü seçerek satın alma işlemi başlatma ve (mock) ödeme akışını tamamlama
- Satın alınan biletlerin listelenmesi ve her bilet için QR kod gösterimi
- Personel (STAFF) için atanmış etkinlik listesi ve QR kod okuyucu ile bilet kontrolü (check-in)
- Access token süresi dolduğunda refresh token ile otomatik yenileme
- Oturum bilgilerinin cihazda yerel depolama katmanında (DataStore) saklanması

## Mimari

Proje, sorumlulukların ayrılması amacıyla çok modüllü bir yapı ile organize edilmiştir:

- **app** — Kullanıcı arayüzü (Jetpack Compose ekranları), navigasyon ve ViewModel katmanı. MVVM mimarisini takip eder.
- **core** — Uygulama genelinde paylaşılan domain modelleri (auth, event, purchase), ortak UI teması ve yardımcı sınıflar. Diğer modüllerin bağımlı olduğu temel modüldür.
- **data** — Uzak API ile iletişim (Retrofit/OkHttp), veri transfer nesneleri (DTO), yerel depolama (DataStore) ve repository implementasyonları.

Bağımlılık enjeksiyonu için Koin kullanılmaktadır; her modülün kendi Koin modülü bulunur ve uygulama başlangıcında birleştirilir.

## Proje Yapısı

```
TicketApp/
├── app/    Compose ekranları, navigasyon, ViewModel'ler, DI
├── core/   Domain modelleri, ortak UI bileşenleri, tema
├── data/   Retrofit servisleri, DTO'lar, repository'ler, yerel depolama
├── gradle/ Sürüm katalogları (libs.versions.toml)
└── API.MD  Sunucu tarafı REST API dokümantasyonu
```

## Kullanılan Teknolojiler

- Kotlin, Jetpack Compose, Material 3
- Koin (bağımlılık enjeksiyonu)
- Retrofit, OkHttp, kotlinx.serialization (ağ katmanı)
- Jetpack Navigation Compose
- Jetpack Lifecycle / ViewModel
- Jetpack DataStore Preferences (yerel depolama)
- ZXing / ZXing Android Embedded (QR kod üretimi ve okuma)

## Gereksinimler

- Android Studio (güncel sürüm önerilir)
- JDK 11
- Android SDK — minimum API 24, hedef/derleme API 36
- Çalışan bir Ticket API sunucusu (bkz. [API.MD](API.MD))

## Kurulum

1. Depoyu klonlayın veya indirin.
2. Projeyi Android Studio ile açın; Gradle senkronizasyonunun tamamlanmasını bekleyin.
3. [Yapılandırma](#yapılandırma) bölümündeki adımlara göre API taban adresini ayarlayın.

## Yapılandırma

Uygulama, sunucu ile iletişim için bir taban URL değerine ihtiyaç duyar. Yerel geliştirme için sunucunun `http://localhost:3000` adresinde çalıştığı varsayılır. Fiziksel cihaz veya farklı bir ortamda test edilecekse, ağ katmanındaki taban adresin ilgili ortamın erişilebilir adresiyle güncellenmesi gerekir.

Geliştirme ortamı için gereken örnek (seed) kullanıcı bilgileri güvenlik nedeniyle bu dokümanda paylaşılmamıştır; sunucu ekibinden veya dahili geliştirme ortamı dokümantasyonundan temin edilmelidir.

## Derleme ve Çalıştırma

Android Studio üzerinden `app` modülünü seçili cihaz veya emülatörde çalıştırabilir, ya da komut satırından aşağıdaki gibi derleme alabilirsiniz:

```
./gradlew assembleDebug
```

Testleri çalıştırmak için:

```
./gradlew test
```

## API Referansı

Uygulamanın tükettiği REST API'nin uç noktaları, kimlik doğrulama akışı, hata formatı ve tipik istemci senaryoları [API.MD](API.MD) dosyasında ayrıntılı olarak açıklanmıştır.

## Güvenlik

- Kimlik doğrulama, sunucu tarafından üretilen JWT erişim (access) ve yenileme (refresh) token çiftine dayanır; korumalı isteklere `Authorization: Bearer <accessToken>` başlığı istemci tarafında otomatik olarak eklenir.
- Access token süresi dolduğunda (401 yanıtı) istemci, refresh token ile yeni bir token çifti alıp isteği otomatik olarak tekrar dener; eşzamanlı isteklerin tekrar tekrar yenileme çağrısı yapmaması için senkronizasyon uygulanır.
- Refresh token rotation uygulanır: her yenileme çağrısında sunucu yeni bir refresh token üretir ve öncekini geçersiz kılar.
- **Bilinen risk:** Access ve refresh token'lar şu anda cihazda şifrelenmemiş Jetpack DataStore Preferences ile düz metin olarak saklanmaktadır. Bu, cihaza fiziksel veya kök (root) erişimi olan biri için token'ların okunmasına imkân tanır. Üretim kullanımı öncesinde bu katmanın Android Keystore destekli şifreli bir depolama mekanizmasıyla değiştirilmesi önerilir.
- Yerel geliştirme ortamı varsayılan olarak şifresiz `http://localhost:3000` adresini kullanır; üretim ortamında bağlantının HTTPS üzerinden yapılması zorunlu tutulmalıdır.
- Ağ isteklerini loglayan araçlar (ör. OkHttp logging interceptor) yalnızca geliştirme derlemelerinde etkin tutulmalı, üretim derlemelerinde şifre ve token gibi hassas bilgilerin loglanması önlenmelidir.

## Bilinen Sınırlamalar

- Gerçek bir ödeme sağlayıcı entegrasyonu bulunmamaktadır; ödeme adımı sunucu tarafında mock olarak işlenir.
- İade veya iptal işlemleri desteklenmemektedir.
- E-posta doğrulama ve şifre sıfırlama akışları bulunmamaktadır.
- Push bildirim desteği yoktur.
- Sunucu tarafı hata mesajları yalnızca Türkçe olarak sunulmaktadır.
