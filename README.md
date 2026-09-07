# Workout

自分専用のワークアウト記録アプリ(Android)。まずは腕立て伏せの自動カウンター＆記録から。

## 特徴

- フロントカメラと [ML Kit Face Detection](https://developers.google.com/ml-kit/vision/face-detection) を使い、腕立て伏せの回数を自動でカウントします。専用センサーや装着物は不要です。
- スマホを床に置いてインカメラを自分に向けるだけで使えます。
- 記録はカレンダー表示。日付ごとの合計回数が一覧でき、日付をタップするとその日のセッション一覧から個別に削除できます。
- 記録は端末内([DataStore](https://developer.android.com/topic/libraries/architecture/datastore))にのみ保存され、外部への送信は一切行いません。

## 使い方

1. スマホを床など安定した場所に置き、インカメラが自分の顔を映すように立てかける
2. ホーム画面で「腕立て伏せを始める」→「開始」
3. 腕立て伏せを行うと、顔とカメラの距離の変化を検知して自動でカウントされる
4. 終わったら「終了して記録する」で保存

## カウントの仕組み

カメラベースの計測方式を採用しています。ML Kit Face Detectionで検出した顔のバウンディングボックスの高さの変化を使い、直近数秒の動的レンジに対する相対位置としてこの値を捉え、ヒステリシス付きの状態遷移でカウントすることで、体格差や設置距離のばらつきを吸収しています(詳細は [`RepCounter.kt`](app/src/main/java/com/tatu/workout/pushup/RepCounter.kt))。

## 技術スタック

- Kotlin / Jetpack Compose
- CameraX(カメラプレビュー・フレーム解析)
- ML Kit Face Detection(顔検出)
- DataStore + kotlinx.serialization(記録の永続化、JSON形式)

## ビルド

```bash
./gradlew assembleDebug
```

個人利用前提のアプリのため、release ビルドも debug 鍵で署名されます(`app/build.gradle.kts` 参照)。ストア配布は想定していません。

## 動作要件

- Android 8.0 (API 26) 以降
- フロントカメラ

## 今後やりたいこと

- 腕立て伏せ以外の種目への対応
- 記録の推移をグラフで見られるようにする
