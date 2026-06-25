# Ascendia

Fabric 1.21.1 için istemci taraflı (client-side) envanter/kasa hız mod'u.
Mod ID: `ascendia` — Paket: `exloran.ascendia`

## Özellikler

### Envanter ekranı (E tuşu)
Envanterin altına simetrik 3 buton eklenir:
- **Düzenle** → Düzenleme modunu açar/kapatır ("Kaydet" olur). Açıkken diğer
  butonları mouse ile sürükleyip istediğin yere taşıyabilirsin, bıraktığın anda
  konum otomatik olarak `config/ascendia.json` içine kaydedilir.
- **Oto Ekipman** → Envanterindeki en iyi zırh setini (Netherite > Elmas > Demir >
  Zincir > Altın > Deri sırasıyla) otomatik olarak üzerine giyer.
- **Herşeyi At** → Envanterdeki, zırh slotlarındaki ve kalkan (offhand) slotundaki
  HER ŞEYİ yere atar.

### Ender Chest
Sandığın sağına simetrik 4 buton eklenir:
- **Herşeyi At** → Sadece Ender Chest içindekileri atar, envanterine dokunmaz.
- **Herşeyi Al** → Ender Chest'teki her şeyi envanterine alır. Yer yoksa
  ekranda kırmızı uyarı çıkar ve kalan eşyalar sandıkta bırakılır.
- **Herşeyi Koy** → Envanterindeki her şeyi açık olan Ender Chest'e koyar.
- **Çöpleri At** → Sadece `config/ascendia.json` içindeki çöp listesine uyan
  eşyaları atar (toprak, ip, zincir vb.). Büyülü (örn. Koruma 1+) hiçbir item
  asla atılmaz — set parçaların güvende. Atılacak çöp yoksa "Atılacak çöp
  bulunamadı." uyarısı çıkar.

### /pv (PlayerVaults benzeri) kasası
Aynı 4 buton (Herşeyi At, Herşeyi Al, Herşeyi Koy, Çöpleri At) PV kasasında da
çalışır. Mod, sandık ekranının BAŞLIĞINA bakarak Ender Chest mi yoksa PV kasası
mı olduğunu ayırt eder; normal sandık ve shulker kutularına hiç dokunmaz.

> **ÖNEMLİ:** PV kasanızın GUI başlığı sunucunuzda farklıysa (örn. "Sandığım 1"
> gibi), `config/ascendia.json` içindeki `pvTitleKeywords` listesine o kelimeyi
> ekleyin — başlıkta bu kelimelerden biri geçiyorsa PV kasası olarak tanınır.

## Config (`config/ascendia.json`)
İlk açılışta otomatik oluşturulur. Düzenlenebilir alanlar:

| Alan | Açıklama |
|---|---|
| `buttonColor`, `buttonHoverColor`, `buttonBorderColor`, `textColor` | ARGB renk kodları (`0xAARRGGBB`) — butonların görünümü |
| `trashItems` | Çöp olarak atılacak item ID listesi |
| `protectEnchantedItems` | `true` ise büyülü itemler asla çöp sayılmaz |
| `pvTitleKeywords` | PV kasasını tanımak için sandık başlığında aranan kelimeler |
| `buttonOffsets` | Düzenle modunda sürüklenen butonların kayıtlı konumları (elle dokunmana gerek yok) |

## Derleme

```bash
./gradlew build
```

Jar dosyası `build/libs/ascendia-1.0.0.jar` içinde oluşur.

### ⚠️ Gradle Wrapper hakkında not
Bu proje paketinde `gradlew` betiklerinin çalışması için gereken
`gradle/wrapper/gradle-wrapper.jar` (binary dosya) **dahil edilmedi**, çünkü bu
ortamda internet erişimi yok. Build'in çalışması için:

1. LuckyAscension projendeki `gradlew`, `gradlew.bat` ve
   `gradle/wrapper/gradle-wrapper.jar` + `gradle-wrapper.properties`
   dosyalarını bu projeye kopyala, **veya**
2. Yerel makinende (Gradle kurulu) `gradle wrapper` komutunu bir kere çalıştır
   ve oluşan dosyaları commit'le.

Bunlardan biri yapılınca GitHub Actions (`.github/workflows/build.yml`) normal
şekilde build alacaktır.

## Bilinen riskler / CI'da hata alırsan
- `gradle.properties` içindeki `yarn_mappings`, `loader_version`,
  `fabric_version` değerleri yaklaşık güncel sürümlerdir; build hata verirse
  fabricmc.net/use üzerinden 1.21.1 için güncel değerlerle değiştir.
- Mixin'ler `InventoryScreen` ve `GenericContainerScreen` sınıflarının
  `init()` metoduna ve `x`/`y`/`backgroundWidth`/`backgroundHeight` alanlarına
  bağımlı (yıllardır stabil Yarn isimleri). Eğer mapping farkı yüzünden mixin
  derleme hatası alırsan, hata mesajını bana ilet, hemen düzeltirim.
- Zırh slot ID'leri (5=kask, 6=göğüslük, 7=tozluk, 8=bot, 45=kalkan) vanilla
  `PlayerScreenHandler`'da uzun süredir sabit; versiyon değişse de büyük
  olasılıkla aynı kalır.

## Önerilen sonraki adım
Bu kodu mevcut LuckyAscension repo yapına benzer şekilde GitHub'a push'la,
Actions sekmesinden build sonucunu kontrol et. Hata çıkarsa log'u buraya
yapıştır, birlikte düzeltelim — yeni bir mod olduğu için bazı ince ayarlar
(buton boyutu, kasa başlığı eşleşmesi vb.) ilk denemede tam oturmayabilir.
