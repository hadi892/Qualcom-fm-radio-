# Qualcomm FM Service Component Layer

The FM Service layer handles background execution, audio focus routing, notification controls, and state management for the Qualcomm FM application.

## Components
- `FmRadioService`: Android Foreground Service supporting background playback and Android 16 Media Session.
- `FmAudioRouter`: Manages wired headset antenna detection and speaker toggles via `AudioManager`.
- `RdsData`: Data model for Program Service (PS) and Radio Text (RT) decoding.
- `FmBand`: Defines US/EU, Japan, and World regional FM band boundaries.
