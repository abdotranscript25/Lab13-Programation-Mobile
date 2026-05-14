# 📍 Lab 13 – Application de Localisation avec OpenStreetMap

## 🎯 Objectif du laboratoire

Créer une application Android complète qui :

| Fonctionnalité | Description |
|----------------|-------------|
| **Capture la position GPS** | Récupère latitude, longitude, altitude et précision |
| **Envoie au serveur** | Stocke les coordonnées dans une base MySQL via PHP |
| **Affiche une carte** | Utilise **OpenStreetMap** (alternatif gratuit à Google Maps) |
| **Affiche des marqueurs** | Toutes les positions enregistrées apparaissent sur la carte |

**Résultat final :** Une application qui vous localise, envoie vos positions à un serveur, et les affiche sur une carte OpenStreetMap.

---

## 🗺️ OpenStreetMap vs Google Maps

| Critère | Google Maps | OpenStreetMap (OSM) |
|---------|-------------|---------------------|
| **Prix** | Payant au-delà du quota gratuit | **Totalement gratuit** |
| **Clé API** | Requise (carte bancaire) | **Aucune clé nécessaire** |
| **Données** | Propriétaires | **Open Data** (collaboratif) |
| **Bibliothèque Android** | Google Maps SDK | **OSMDroid** |
| **Licence** | Propriétaire | **Open Source** |

> **Pourquoi OSM ?** Alternative gratuite et open-source, parfaite pour l'apprentissage.

---

## 🧠 Concepts clés abordés

### 1. OSMDroid (OpenStreetMap pour Android)

| Concept | Explication |
|---------|-------------|
| **OSMDroid** | Bibliothèque Android gratuite pour afficher des cartes OpenStreetMap |
| **MapView** | Vue qui affiche la carte (équivalent du `SupportMapFragment`) |
| **TileSource** | Source des tuiles cartographiques (`MAPNIK` = style standard) |
| **GeoPoint** | Point géographique (latitude, longitude) → remplace `LatLng` |
| **Marker** | Repère visuel sur la carte |

**Initialisation obligatoire :**
```java
Configuration.getInstance().load(getApplicationContext(),
        getSharedPreferences("osmdroid_prefs", MODE_PRIVATE));
```

### 2. LocationManager (GPS)

| Concept | Explication |
|---------|-------------|
| **LocationManager** | Service système pour accéder à la localisation |
| **LocationListener** | Interface qui réagit aux changements de position |
| **GPS_PROVIDER** | Localisation précise via satellite |
| **NETWORK_PROVIDER** | Localisation approximative via WiFi/4G |

### 3. Volley (Requêtes HTTP)

| Concept | Explication |
|---------|-------------|
| **Volley** | Bibliothèque réseau de Google |
| **StringRequest** | Requête HTTP retournant une chaîne (pour POST) |
| **JsonObjectRequest** | Requête HTTP retournant directement du JSON |

### 4. PHP/MySQL (Backend)

| Concept | Explication |
|---------|-------------|
| **PDO** | Interface PHP pour accéder à MySQL (sécurisée) |
| **Requête préparée** | Protège contre les injections SQL |
| **JSON** | Format d'échange entre Android et serveur |

---

## 📊 Correspondances Google Maps → OpenStreetMap

| Google Maps | OpenStreetMap (OSMDroid) |
|-------------|--------------------------|
| `com.google.android.gms.maps.MapView` | `org.osmdroid.views.MapView` |
| `com.google.android.gms.maps.model.LatLng` | `org.osmdroid.util.GeoPoint` |
| `com.google.android.gms.maps.model.Marker` | `org.osmdroid.views.overlay.Marker` |
| Clé API requise | **Aucune clé API** |
| Payant | **Totalement gratuit** |

---

## 🏗️ Architecture du projet (Pipeline)

```
┌──────────────────────────────────────────┐
│           📱 MainActivity                │
│  • GPS capture la position               │
│  • Envoie à createPosition.php           │
└──────────────────────┬───────────────────┘
                       │ POST (lat, lon, date, imei)
                       ▼
┌──────────────────────────────────────────┐
│        🌐 createPosition.php             │
│  • Reçoit les données                    │
│  • INSERT dans table positions           │
└──────────────────────┬───────────────────┘
                       │ Stockage
                       ▼
┌──────────────────────────────────────────┐
│       🗄️ MySQL (table positions)         │
│  id │ latitude │ longitude │ date │ imei │
└──────────────────────┬───────────────────┘
                       │ SELECT * FROM positions
                       ▼
┌──────────────────────────────────────────┐
│         🌐 getPosition.php               │
│  • Récupère toutes les positions         │
│  • Retourne JSON : {"positions":[...]}   │
└──────────────────────┬───────────────────┘
                       │ Volley (JsonObjectRequest)
                       ▼
┌──────────────────────────────────────────┐
│        🗺️ GoogleMapActivity              │
│  • Reçoit le JSON                        │
│  • Pour chaque position → marqueur       │
│  • Carte OpenStreetMap (OSMDroid)        │
└──────────────────────────────────────────┘
```

---

## 🔄 Flux des données (résumé)

| Étape | Composant | Action |
|-------|-----------|--------|
| 1 | `MainActivity` | Capture GPS et **envoie** |
| 2 | `createPosition.php` | **Stocke** dans MySQL |
| 3 | `getPosition.php` | **Récupère** toutes les positions |
| 4 | `GoogleMapActivity` | **Affiche** les marqueurs sur la carte |

---

## 🛠️ Technologies utilisées

| Catégorie | Technologie | Version |
|-----------|-------------|---------|
| IDE | Android Studio | Dernière |
| Langage | Java | 8+ |
| Carte | OSMDroid (OpenStreetMap) | 6.1.18 |
| Réseau | Volley | 1.2.1 |
| Serveur | XAMPP / WAMP | - |
| Base de données | MySQL | 5.7+ |
| Backend | PHP | 7.4+ |

---

## 📂 Structure du projet Android

```
MapApplication/
├── java/com.example.mapapplication/
│   ├── MainActivity.java               # GPS + envoi au serveur
│   └── GoogleMapActivity.java          # Carte + marqueurs
├── res/
│   ├── layout/
│   │   ├── activity_main.xml           # Layout principal
│   │   └── activity_google_map.xml     # Layout carte
│   ├── drawable/
│   │   └── location_pin.png            # Icône du marqueur
│   └── xml/
│       └── network_security_config.xml # Configuration HTTP
├── manifests/
│   └── AndroidManifest.xml             # Permissions
└── build.gradle.kts                    # Dépendances
```

---

## 📂 Structure du projet serveur (PHP/MySQL)

```
E:\Xampp\htdocs\map_project\
├── createPosition.php                  # INSERT (réception GPS)
└── getPosition.php                     # SELECT (envoi JSON)
```

<img width="1137" height="822" alt="createPosition1" src="https://github.com/user-attachments/assets/0edca608-fa89-4852-a605-7b57158a8535" />
<img width="792" height="266" alt="createPosition2" src="https://github.com/user-attachments/assets/be67096a-c1f4-4ce7-bc90-2854c55d1ce1" />

<img width="858" height="661" alt="getPosition" src="https://github.com/user-attachments/assets/d40819fc-497d-4d2d-8247-9fe52c3b0862" />


---

## 🗄️ Base de données MySQL

### Structure de la table `positions`

| Champ | Type | Description |
|-------|------|-------------|
| `id` | INT | Identifiant unique (auto-incrémenté) |
| `latitude` | DOUBLE | Coordonnée Nord-Sud |
| `longitude` | DOUBLE | Coordonnée Est-Ouest |
| `date` | DATETIME | Date et heure d'envoi |
| `imei` | VARCHAR(50) | Identifiant du téléphone (Android ID) |


<img width="1498" height="387" alt="DB" src="https://github.com/user-attachments/assets/88434447-7ad7-4aac-b0d1-f83e27b1ed31" />



### Script SQL de création

```sql
CREATE DATABASE IF NOT EXISTS map_project;
USE map_project;

CREATE TABLE IF NOT EXISTS `positions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `date` datetime NOT NULL,
  `imei` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

---

## 📱 Fonctionnalités de l'application

| Fonctionnalité | Statut |
|----------------|--------|
| Capture GPS en temps réel | ✅ |
| Envoi au serveur (POST) | ✅ |
| Stockage en base MySQL | ✅ |
| Carte OpenStreetMap (OSMDroid) | ✅ |
| Chargement des positions depuis serveur | ✅ |
| Affichage des marqueurs sur la carte | ✅ |
| Compteur du nombre de positions | ✅ |
| Gestion des permissions runtime | ✅ |
| Configuration réseau (HTTP) | ✅ |

---

## 💻 Extraits de code importants

### Initialisation OSMDroid — `GoogleMapActivity.java`

```java
// Configuration obligatoire
Configuration.getInstance().load(getApplicationContext(),
        getSharedPreferences("osmdroid_prefs", MODE_PRIVATE));

// Configuration de la carte
mapView.setTileSource(TileSourceFactory.MAPNIK); // ← OpenStreetMap
mapView.setBuiltInZoomControls(true);
mapView.setMultiTouchControls(true);
mapView.getController().setZoom(14.0);
mapView.getController().setCenter(new GeoPoint(31.5442, -8.7709));
```

### Chargement des marqueurs — `GoogleMapActivity.java`

```java
private void chargerPositions() {
    JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST,
            showUrl,
            null,
            response -> {
                JSONArray positions = response.getJSONArray("positions");
                for (int i = 0; i < positions.length(); i++) {
                    JSONObject pos = positions.getJSONObject(i);
                    double lat = pos.getDouble("latitude");
                    double lon = pos.getDouble("longitude");
                    ajouterMarqueur(lat, lon, "Position #" + (i + 1), "");
                }
                mapView.invalidate();
            },
            error -> { /* Gestion erreur */ }
    );
    requestQueue.add(request);
}
```


<img width="944" height="790" alt="GoogleMapActivity1" src="https://github.com/user-attachments/assets/472f58c7-7c65-4603-a494-455a98eed7d1" />
<img width="928" height="795" alt="GoogleMapActivity2" src="https://github.com/user-attachments/assets/84e4e4cd-4adc-488c-a6ed-bb2d47f2cc01" />
<img width="1123" height="794" alt="GoogleMapActivity3" src="https://github.com/user-attachments/assets/8987bb4b-01b7-45cc-8302-75a38b713c77" />
<img width="1054" height="795" alt="GoogleMapActivity4" src="https://github.com/user-attachments/assets/91d0bc7d-d775-4fd4-89f4-f0bda47e732f" />
<img width="819" height="382" alt="GoogleMapActivity5" src="https://github.com/user-attachments/assets/71ed9d12-9671-4f4b-9cf0-9f058839919f" />




### Envoi de la position — `MainActivity.java`

```java
private void envoyerPosition(final double lat, final double lon) {
    StringRequest request = new StringRequest(
            Request.Method.POST,
            insertUrl,
            response -> { /* Succès */ },
            error -> { /* Erreur */ }
    ) {
        @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
            params.put("latitude", String.valueOf(lat));
            params.put("longitude", String.valueOf(lon));
            params.put("date", sdf.format(new Date()));
            String androidId = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID);
            params.put("imei", androidId);
            return params;
        }
    };
    requestQueue.add(request);
}
```

<img width="1104" height="793" alt="MainActivity1" src="https://github.com/user-attachments/assets/7accfc56-b1c1-42df-a044-be1f0efeb5d8" />
<img width="987" height="799" alt="MainActivity2" src="https://github.com/user-attachments/assets/d2abfeb7-f41d-43de-9fa0-fb47321e684f" />
<img width="1066" height="795" alt="MainActivity3" src="https://github.com/user-attachments/assets/e2c25cba-e067-4f39-9765-226f60ba6077" />
<img width="1192" height="791" alt="MainActivity4" src="https://github.com/user-attachments/assets/f9083037-bbc8-4d77-b01b-d10a464d0ed4" />
<img width="1197" height="725" alt="MainActivity5" src="https://github.com/user-attachments/assets/b98b2c9b-dcd7-431c-b43b-5d4d6df94a44" />





---

## 🔧 Configuration réseau (pour HTTP)

### `network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168.1.XX</domain>
    </domain-config>
</network-security-config>
```

### `AndroidManifest.xml` (extrait)

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="true"
    ... >
```


<img width="910" height="786" alt="Manifest" src="https://github.com/user-attachments/assets/1b72e390-7218-4fe6-ba4d-b2d94ae501d1" />



---

## 🔧 Installation et exécution

### Prérequis serveur
- XAMPP / WAMP installé
- PHP 7.4+ et MySQL
- Téléphone et PC sur le **même réseau Wi-Fi**


---

## Test et démonstration :


<img width="328" height="695" alt="test1" src="https://github.com/user-attachments/assets/b6098dbd-4460-42d2-acbe-b5829e8544be" />
<img width="327" height="691" alt="test2" src="https://github.com/user-attachments/assets/fb128a7e-e852-486a-b682-e0c50e680678" />




https://github.com/user-attachments/assets/fd09d6ea-d042-467e-b8e6-c46d2b613786



## 🐛 Résolution des erreurs courantes

| Erreur | Solution |
|--------|----------|
| `Cleartext HTTP traffic not permitted` | Ajouter `network_security_config.xml` et `usesCleartextTraffic="true"` |
| `ClassCastException: VectorDrawable cannot be cast to BitmapDrawable` | Utiliser une image PNG ou supprimer `setIcon()` |
| `ActivityNotFoundException` | Déclarer `GoogleMapActivity` dans `AndroidManifest.xml` |
| `Cannot resolve symbol 'marker'` | Vérifier le nom de l'image dans `res/drawable/` |
| `Failed to connect to /192.168.x.x` | Vérifier que téléphone et PC sont sur le même Wi-Fi |

---

## 📚 RÉCAPITULATIF – CE QUE J'AI APPRIS

---

### ✅ Synthèse du laboratoire

Ce laboratoire m'a permis de maîtriser l'utilisation d'**OpenStreetMap** comme alternative gratuite à Google Maps, la communication **Android ↔ serveur PHP/MySQL** avec Volley, et l'affichage de **marqueurs dynamiques** chargés depuis une base de données.

---

### 📝 Les 8 points essentiels à retenir

| # | Point clé |
|---|-----------|
| 1 | **OSMDroid** = OpenStreetMap pour Android, sans clé API ni frais |
| 2 | **`Configuration.getInstance().load()`** est obligatoire avant tout usage d'OSMDroid |
| 3 | **`GeoPoint`** remplace `LatLng` (Google Maps) dans OSMDroid |
| 4 | **`MAPNIK`** est la source de tuiles standard d'OpenStreetMap |
| 5 | **`StringRequest`** pour envoyer (POST), **`JsonObjectRequest`** pour recevoir du JSON |
| 6 | **`network_security_config.xml`** est obligatoire pour les requêtes HTTP (non-HTTPS) en Android 9+ |
| 7 | **`Settings.Secure.ANDROID_ID`** remplace l'IMEI (plus besoin de permissions sensibles) |
| 8 | **`mapView.invalidate()`** force le redessinage de la carte après ajout de marqueurs |

---

### 📊 Comparaison Lab 12 vs Lab 13

| Aspect | Lab 12 (Google Maps) | Lab 13 (OpenStreetMap) |
|--------|----------------------|------------------------|
| Bibliothèque carte | Google Maps SDK | OSMDroid |
| Clé API | ✅ Requise | ❌ Non nécessaire |
| Coût | Payant (quota) | Totalement gratuit |
| Classe point | `LatLng` | `GeoPoint` |
| Licence | Propriétaire | Open Source |

---

### 💡 Bonnes pratiques retenues

- [x] Toujours initialiser OSMDroid avec `Configuration.getInstance().load()` dans `onCreate()`
- [x] Appeler `mapView.onResume()` / `mapView.onPause()` dans les méthodes du même nom
- [x] Utiliser `mapView.invalidate()` après chaque modification des overlays
- [x] Stocker l'adresse IP du serveur dans une constante pour faciliter la modification
- [x] Tester les endpoints PHP avec Postman avant l'intégration Android

---

### 🎯 Compétences acquises

| Compétence | Niveau |
|------------|--------|
| Intégrer OpenStreetMap avec OSMDroid | ✅ Maîtrisé |
| Ajouter des marqueurs dynamiques sur la carte | ✅ Maîtrisé |
| Capturer la position GPS avec LocationManager | ✅ Maîtrisé |
| Envoyer des données POST avec Volley | ✅ Maîtrisé |
| Parser une réponse JSON depuis le serveur | ✅ Maîtrisé |
| Configurer un backend PHP/MySQL | ✅ Maîtrisé |
| Gérer la configuration réseau HTTP (Android 9+) | ✅ Maîtrisé |

---

### ✅ Vérification finale

- [x] La position GPS est capturée et affichée
- [x] Les données sont insérées dans la table `positions`
- [x] La carte OpenStreetMap s'affiche
- [x] Les marqueurs apparaissent pour chaque position en base
- [x] Le compteur de positions fonctionne
- [x] Les contrôles de zoom fonctionnent

---

### 👨‍💻 Auteur

| Élément | Information |
|---------|-------------|
| **Nom** | El Hachimi Abdelhamid |
| **GitHub** | [abdotranscript25](https://github.com/abdotranscript25) |
| **Lab** | Programmation Mobile - Lab 13 |

---

### 📅 Version

| Élément | Information |
|---------|-------------|
| **Date** | Mai 2026 |
| **Version** | 1.0 |
| **Statut** | ✅ Finalisé |
