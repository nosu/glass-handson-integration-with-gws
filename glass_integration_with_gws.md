id: glass_integration_with_gws
status: [draft]
author: Wataru Inoue
summary: Google Workspace と連携したアプリの開発
categories: Android, Glass
tags: android, googleglass, gws, workspace
feedback link: TBE

Google Workspace と連携したアプリの開発
==================================

コースの概要
----------

### はじめに

このコースでは、Google Glass Enterprise Edition 2 向けに、Google Workspace (以下 `GWS`) と連携するアプリケーションの開発を行います。
Glass 用のアプリケーションは、オフラインで完結するものを開発することも可能ですが、GWS に含まれる Google Drive / Google Calendar / Google Slides 等の様々なクラウドサービスと連携することで、より便利な機能を実現することができます。

今回は、Google Slides と連携して、PC 等で作成したスライド（プレゼンテーション）を、Glass の画面に表示し、スワイプ操作でページめくりを行うことができるアプリケーションを開発してみます。

![プロジェクト選択](img/app_capture.gif)

### 必要なもの
- Google Glass Enterprise Edition 2
- 以下がインストールされた PC
  - Android Studio
  - git
  - Android Emulator（Google Glass Enterprise Edition 2 がない場合）
- Google Cloud アカウント
- Google Workspace アカウント


### 必要な事前知識

このコースでは、以下の事前知識を前提としています。
- Android Studio の基本操作
- Google Glass Enterprise Edition 2 (またはエミュレータ)の操作方法
- Android アプリケーション開発の基礎
- Java の基本的な文法


### このコースで学ぶこと

- Google API Client ライブラリによる Google API の利用
- Fragment や Layout の作成方法
- Android における非同期処理の実装方法

### このコースでやること

`CardSample` プロジェクトをベースに、Google Slides と連携して、PC 等で作成したスライド（プレゼンテーション）を、Glass の画面に表示し、スワイプ操作でページめくりを行うことができるアプリケーションを開発してみます。

前半では、Slides API をはじめとするクラウド側の準備作業を行います。
後半では、 `CardSample` をベースとして、スライドを表示するための実装を行います。




Google Cloud / GWS 側の準備
--------------------------

まずは、アプリから Slides API を呼び出すことができるように、クラウドサービス側の準備を行います。

- Google Cloud プロジェクトの作成・選択
- Slides API の有効化
- サービスアカウントの作成と権限の付与
- サービスアカウントの鍵を作成・ダウンロード
- テスト用スライドの準備

### Google Cloud プロジェクトの作成・選択

今日のハンズオン用に Google Cloud のプロジェクトを作成します。

- Cloud Console ([https://console.cloud.google.com](https://console.cloud.google.com)) にアクセスする
- ヘッダにあるプロジェクト名（ここでは `glass-handson`）をクリックし、表示されたダイアログから「新しいプロジェクト」を選択
![ヘッダ](img/console_header.png)
<!-- ![プロジェクト選択](img/select_project.png) -->
- 適当なプロジェクト名を入力し、「作成」をクリック
![プロジェクト選択](img/create_new_project.png)
- 作成完了の通知が表示されたら、通知ポップアップから作成したプロジェクトに切り替える

> aside positive
> プロジェクトを作成せず、作成済みのプロジェクトをそのまま利用いただいても構いません。その場合は、ヘッダにあるプロジェクト名をクリックしてから、使用したいプロジェクトを選択してください。

### Slides API の有効化

Google Cloud では、意図しない API の利用を防ぐために、デフォルトではすべての API が無効化されています。
今回開発するアプリでは Slides API を利用したいので、Slides API を明示的に有効化します。

- Console 画面上部にある検索窓に、”slides api” などと入力し、表示された候補から「Google Slides API」を選択する
- 「有効にする」をクリックしてしばらく待つ

### サービスアカウントの作成と権限の付与

- Console の左メニューから「IAM と管理」→「サービスアカウント」をクリック
- 「＋ サービスアカウントを作成」をクリック
- 以下のとおり入力して、`完了` をクリック（あるいは違う名前でも構いません）
  - サービスアカウント名: `glass-slides-app`
  - サービスアカウントID: `glass-slides-app`
- サービスアカウント一覧画面に戻ったら、今作成したサービスアカウントのメールアドレス（`glass-slides-app@<ProjectID>.iam.gserviceaccount.com`）をコピーしてメモしておく


### サービスアカウントの鍵を作成・ダウンロード

次に、作成したサービスアカウントで認証を行うためのキーファイルをダウンロードします。

- サービスアカウント一覧画面で、先ほど作成した `glass-slides-app@<ProjectID>.iam.gserviceaccount.com` の列の右側にある `操作` という列の `…` アイコンをクリック
- `鍵を管理` をクリック
- `鍵を追加` をクリックし、`新しい鍵を作成` をクリック
- `キーのタイプ` として `JSON` が選択されていることを確認し、`作成` をクリック
- 鍵が作成されて、自動的に鍵の JSON ファイルがダウンロードされる



### テスト用スライドの準備

アプリから表示してみるためのテスト用スライドを用意します。

- [Google Slides](https://docs.google.com/presentation/u/0/?tgif=d) にアクセスし、新規スライドを作成する
> aside positive
> ブラウザから [slides.new](https://slides.new/) にアクセスすることでも新しいスライドを作成できます
- 作成したスライドに新しいページを何枚か追加し、適当な内容を記載する

### サービスアカウントにテスト用スライドへの閲覧権限を付与

- 画面右上の `Share`（`共有`）ボタンをクリック
- 入力欄に、先ほど作成したサービスアカウントのメールアドレス（`glass-slides-app@<ProjectID>.iam.gserviceaccount.com`）を入力して、`Viewer`（`閲覧者`）を選択してから `Send`（`送信`）をクリックする

以上でクラウド側の準備は完了です。


アプリ実装(1) Android Project の準備
---------------------------------

クラウドサービスの準備ができたので、いよいよアプリ側の実装を進めていきます。

### ベースとなるプロジェクトの準備

今回は、公式の Glass 用サンプルアプリ集にある `CardSample` というアプリをベースに実装を行います。
リポジトリをクローンし、Android Studio で開いてみましょう。

- 以下のコマンドで Git リポジトリをローカルに Clone する
```bash
git clone https://github.com/googlesamples/glass-enterprise-samples.git
```
- Android Studio で File -> Open をクリックし、 `glass-enterprise-samples/CardSample` を選択して開く

### CardSample アプリの構成確認

`CardSample` アプリでは、`MainActivity` 上にテキストを表示する `Fragment` を複数追加し、左右スワイプで


![CardSampleのアーキテクチャ](img/card_sample_architecture.png)


アプリ実装(2) ライブラリや設定の追加
---------------------------

引き続き、`CardSample` Project に対して、必要なライブラリや設定を追加していきます。

### Google API Client ライブラリの追加

Android Studio のビルドツールである Gradle の設定ファイル（build.gradle）に、Slides API 用ライブラリと認証用ライブラリを依存関係として追記します。

- CardSample/app/build.gradle に以下の2行を追記する
```gradle
dependencies {
    ...
    implementation 'com.google.apis:google-api-services-slides:v1-rev20210820-1.32.1'
    implementation 'com.google.auth:google-auth-library-oauth2-http:1.2.0'
}
```

- Android Studio の右ペイン上部に以下のような警告が出てくるので、”Sync Now” をクリックして、変更を反映する
![Gradle の警告](img/gradle_sync_now.png)


> aside positive
> #### Google API Client ライブラリとは
> Google API は REST API として提供されているので、当然生の HTTP Request を送って操作することも可能ではありますが、受け取るレスポンスの型定義をはじめ、実装に少し手間がかかります。
> Google API Client ライブラリでは、使いやすい
> Google API Client ライブラリは、Java だけではなく Python, .NET, JavaScript, Go, PHP 等、様々な言語向けに提供されています。
> https://developers.google.com/api-client-library?hl=ja


### インターネット接続の Permission 追加

Android からインターネットに接続する場合、Manifest の中で宣言する必要があります。元の `CardSample` アプリはインターネット接続を必要としないアプリでしたが、Slides API に接続するためには Permission を追加する必要があります。

- `CardSample/app/src/main/AndroidManifest.xml` を開き、以下の1行を追記する
```XML
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.android.glass.cardsample">

  <!-- 以下の1行を追加 -->
  <uses-permission android:name="android.permission.INTERNET"/>

  <application
      android:allowBackup="true"
      android:icon="@mipmap/ic_launcher"
      android:label="@string/app_name"
      android:roundIcon="@mipmap/ic_launcher"
      android:supportsRtl="true"
      android:theme="@style/AppTheme">
    ...
  </application>

</manifest>
```


アプリ実装(2) Fragment の実装
---------------------------

続いて、スライドを表示するためのビューとなる Fragment と、そのレイアウト定義を作成していきます。
最終的には、作成した Fragment をスライドのページ数分生成し、`MainActivity` の上に表示し、左右スワイプで切り替えられるようにします。

図で説明すると、もともとの `CardSample` は以下のような画面構成になっていました。
`MainActivity` 上の `ViewPager` で `MainLayoutFragment` という文字列を表示する Fragment を表示していました。

![CardSample のアーキテクチャ](img/card_sample_architecture.png)

これに手を加え、新たに `ImageFragment` という、スライドを表示するための `Fragment` を作成して差し替えることにします。差し替え後のイメージは以下のとおりです。

![開発するアプリのアーキテクチャ](img/app_architecture.png)


### スライド画像表示用の Fragment の作成

スライドの各ページを全画面で表示するための Fragment を作成します。

- 左ペインの Project Tree から、`CardSample/app/src/main/java/com.example.android.glass.cardsample/fragments` を右クリックし、New → Fragment → Fragment (Blank) をクリックする
![Fragment の新規作成](img/create_new_fragment.png)
- 以下のとおり入力して Finish をクリック
  - Fragment Name: ImageLayoutFragment
  - Fragment Layout Name: image_layout
  - Source Language: Java


#### Fragment の画面 Layout の編集

Fragment のレイアウトに ImageView を配置します。

- 先ほどの ImageLayoutFragment 作成に伴って作成された `CardSample/app/src/main/res/res/layout/image_layout.xml` をダブルクリックして開く
- 右ペイン上部のメニューから `Design` → `Code` に切り替える
- XML から `TextView` のタグを削除し、代わりに以下のような `ImageView` を追加する
```XML
<FrameLayout ...>
    ...

    <!-- TextView は削除 -->
    <!-- <TextView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:text="@string/hello_blank_fragment" /> -->

    <!-- ImageView を追加 -->
    <ImageView
        android:id="@+id/slideImageView"
        android:layout_width="640dp"
        android:layout_height="360dp" />
</FrameLayout>
```

アプリ実装(3) サービスアカウントによる認証処理の実装
------------------------------------------

サービスアカウントの JSON 形式のキーファイルをソースコードに追加し、アプリケーション内で読み込んで認証クレデンシャルとして使用できるようにします。

### キーファイルを Asset としてソースコードに追加

まずは、キーの JSON ファイルを Assets としてソースコードに追加します。

- Android Studio 左ペインの Project ツリーから、`CardSample/app/src/main` を右クリックし、New -> Folder -> Assets をクリックする
- 表示されたダイアログで `Finish` をクリックすると、`main` 配下に `Assets` フォルダが作成される
- Cloud Console からダウンロードしたサービスアカウントのキーファイルを、作成した `Assets` フォルダにドラッグアンドドロップする

### キーファイルを読み込む処理の実装

- `MainActivity` をダブルクリックして開く
- キーファイルを読み込んで `com.google.auth.oauth2.GoogleCredentials` として返す `loadServiceAccountCredential()` メソッドを以下のように実装する（キーファイルのファイル名は適宜書き換える）
```Java
...
import com.google.api.services.slides.v1.SlidesScopes;
import com.google.auth.oauth2.GoogleCredentials;
import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
...

public class MainActivity extends BaseActivity {
    private final String SERVICE_ACCOUNT_FILENAME = "{キーファイルのファイル名(e.g. foo.json)}>";

    ...

    private GoogleCredentials loadServiceAccountCredential() throws IOException {
        Collection<String> scopes = new ArrayList<String>();
        scopes.add(SlidesScopes.PRESENTATIONS_READONLY);
        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open(SERVICE_ACCOUNT_FILENAME);
        GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
        return credentials;
    }
}
```

> aside positive
> `android.content.res.AssetManager` を利用することで、Assets フォルダ内のファイルをソースコードから簡単に読み込むことができます

> aside positive
> Google API の認証においては、利用したい API の種類を `Scope` として必ず指定する必要があります。今回はスライドの読み取りのみを行いたいので、Slides API Client に含まれる `SlidesScopes.PRESENTATIONS_READONLY` という定数を `Scope` に追加しています。この定数の実際の値は `https://www.googleapis.com/auth/presentations.readonly` というもので、もちろんこの文字列を直接 `Scope` として追加しても問題ありません。
> 
> 参考）[Google API の Scope 一覧](https://developers.google.com/identity/protocols/oauth2/scopes)


アプリ実装(4) API からデータを取得する非同期処理の実装
----------------------------------------------

Google API Client を使い、Slides API からデータを取得するための処理を記述していきます。
Android アプリでは、HTTP 通信等の時間のかかる処理は、メインスレッド（UI スレッド）ではなく、ワーカースレッドで非同期に実行する必要があります。
今回は、非同期処理を実装する方法として `AsyncTask` を継承したクラスを作成していきます。

> aside negative
> `AsyncTask` クラスは、Android 11（API Level 30）で `非推奨（Deprecated）` 扱いとなりました。今後は代わりの方法として、Kotlin Coroutine などを利用することが推奨されます。本コースでは、Glass の OS が Android 8.1（API Level 27）であることを踏まえて `AsyncTask` を利用しています。


### AsyncTask の

AsyncTask を継承するクラスのインスタンスを作成して `execute()` メソッドを実行すると、`doInBackground()` メソッドに記述した処理が開始されます。
その後、非同期処理が完了すると、`onPostExecute()` に記述した処理が実行されます。

今回の場合、`doInBackground()` において、`Slides API` からスライドの各ページの画像を取得する処理を行います。
また、完了後には AsyncTask を呼び出す Activity 側で、取得した画像を使って Fragment を生成する処理を行います。

従って、Slides API にアクセスする `SlidesApiTask` クラスは以下のようなコードになります。

```Java
public class SlidesApiTask extends AsyncTask<Void, Void, List<Bitmap>> {
    @Override
    protected List<Bitmap> doInBackground(Void... args) {
        // スライドの各ページを取得する処理
    }

    @Override
    protected void onPostExecute(List<Bitmap> result) {
        // API からのページ取得完了後にやりたい処理
    }
}
```

今回、API からのページ取得完了後には、取得したページの画像を配置した Fragment を生成して`MainActivity` のメンバ変数に格納するなど、`MainActivity` に関連した処理を行う必要があります。
これを実現するために、`Listener` という interface を定義し、`SlidesApiTask` のコンストラクタで `Listener` を渡し、処理完了後の `onPostExecute()` において `Listener#onSuccess` を呼び出すようにします。

```Java
public class SlidesApiTask extends AsyncTask<Void, Void, List<Bitmap>> {
    private Listener listener;

    @Override
    protected List<Bitmap> doInBackground(Void... args) {
        // スライドの各ページを取得する処理
    }

    @Override
    protected void onPostExecute(List<Bitmap> result) {
        if (listener != null) {
            listener.onSuccess(result);
        }
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onSuccess(List<Bitmap> result);
    }
}
```

`MainActivity` から `SlideApiTask` を呼び出す場合には、完了時に行いたい処理を記述した `Listener` を作成し、`SlideApiTask` のコンストラクタに渡します。

```Java
public class MainActivity extends BaseActivity {
    ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            SlidesApiTask.Listener listener = createSlidesApiTaskListener();
            slidesApiTask = new SlidesApiTask(listener);
            slidesApiTask.execute();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ...

    private SlidesApiTask.Listener createSlidesApiTaskListener() throws GeneralSecurityException, IOException {
        return new SlidesApiTask.Listener() {
            @Override
            public void onSuccess(List<Bitmap> thumbnails) {
                // MainActivity 側でやりたいこと
                // （あとで実装します）
            }
        };
    }
```


### Slides API Client ライブラリの使い方

TODO: 余裕あれば


### SlidesApiTask の実装

`SlidesApiTask` に、実際のデータ取得処理を実装していきます。
最終的な実装は以下のようになります。

```Java
package com.example.android.glass.cardsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.model.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class SlidesApiTask extends AsyncTask<Void, Void, List<Bitmap>> {
    private Listener listener;
    private GoogleCredentials credentials;
    private String presentationId;
    private final HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public SlidesApiTask(String presentationId, GoogleCredentials credentials) throws GeneralSecurityException, IOException {
        this.presentationId = presentationId;
        this.credentials = credentials;
    }

    @Override
    protected List<Bitmap> doInBackground(Void... args) {
        List<Bitmap> thumbnails = new ArrayList<Bitmap>();
        try {
            List<String> thumbnailUrls = getThumbnailUrls();
            for(int i = 0; i < thumbnailUrls.size(); i++) {
                Bitmap bitmap = getBitmapFromURL(thumbnailUrls.get(i));
                thumbnails.add(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbnails;
    }

    @Override
    protected void onPostExecute(List<Bitmap> result) {
        if (listener != null) {
            listener.onSuccess(result);
        }
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onSuccess(List<Bitmap> result);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> getThumbnailUrls() throws IOException {
        List<String> thumbnailUrls = new ArrayList<String>();

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        Slides slides = new Slides.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName("glass-handson")
                .build();
        Presentation presentation = slides.presentations().get(presentationId).execute();
        List<Page> pages = presentation.getSlides();
        for(int i = 0; i < pages.size(); ++i){
            Page page = pages.get(i);
            Thumbnail thumbnail = slides.presentations().pages().getThumbnail(presentationId, page.getObjectId()).execute();
            thumbnailUrls.add(thumbnail.getContentUrl());
        }

        Log.d("SlidesApiTask", "got thumbnailUrls successfully");
        Log.d("SlidesApiTask", thumbnailUrls.toString());

        return thumbnailUrls;
    }
}
```



## アプリ実装(5) MainActivity の残りの実装


### `SlideApiTask` に渡す `Listener` の実装

`createSlidesApiTaskListener()` に、非同期データ取得完了時の処理を記述していきます。
`MainActivity` クラスの `fragments` 変数に、取得したページ画像を載せた `ImageLayoutFragment` を格納していきます。

```Java
public class MainActivity extends BaseActivity {
    private List<BaseFragment> fragments = new ArrayList<>();

    ...

    private SlidesApiTask.Listener createSlidesApiTaskListener() throws GeneralSecurityException, IOException {
        return new SlidesApiTask.Listener() {
            @Override
            public void onSuccess(List<Bitmap> thumbnails) {
                setContentView(R.layout.view_pager_layout);

                final ScreenSlidePagerAdapter screenSlidePagerAdapter = new ScreenSlidePagerAdapter(
                        getSupportFragmentManager());
                viewPager = findViewById(R.id.viewPager);
                viewPager.setAdapter(screenSlidePagerAdapter);

                for(int i = 0; i < thumbnails.size(); i++) {
                    fragments.add(ImageLayoutFragment
                            .newInstance(thumbnails.get(i)));
                }

                screenSlidePagerAdapter.notifyDataSetChanged();

                final TabLayout tabLayout = findViewById(R.id.page_indicator);
                tabLayout.setupWithViewPager(viewPager, true);
            }
        };
    }
}
```

### `OnCreate()` から `SlidesApiTask` を実行

- スライド

```Java
public class MainActivity extends BaseActivity {
    ...
    private SlidesApiTask slidesApiTask;
    private final String PRESENTATION_ID = "...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleCredentials credentials = loadServiceAccountCredential();
        SlidesApiTask.Listener listener = createSlidesApiTaskListener();
        slidesApiTask = new SlidesApiTask(listener, PRESENTATION_ID, credentials);
        slidesApiTask.execute();
    }

    ...
}
```

最終形の `MainActivity` は以下のようになります。

```Java
/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.glass.cardsample;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.example.android.glass.cardsample.fragments.BaseFragment;
import com.example.android.glass.cardsample.fragments.ImageLayoutFragment;
import com.example.glass.ui.GlassGestureDetector.Gesture;
import com.google.android.material.tabs.TabLayout;
import com.google.api.services.slides.v1.SlidesScopes;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Main activity of the application. It provides viewPager to move between fragments.
 */
public class MainActivity extends BaseActivity {
    private List<BaseFragment> fragments = new ArrayList<>();
    private ViewPager viewPager;
    private SlidesApiTask slidesApiTask;

    private final String PRESENTATION_ID = "128nf3EGQfrQlrmeNfTDtgXJmAMNfZfh-o7hYLlQMKRY";
    private final String SERVICE_ACCOUNT_FILENAME = "glass-handson-bd7e889aa9f0.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loading_layout);

        try {
            GoogleCredentials credentials = loadServiceAccountCredential();
            slidesApiTask = new SlidesApiTask(PRESENTATION_ID, credentials);
            slidesApiTask.setListener(createSlidesApiTaskListener());
            slidesApiTask.execute();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onGesture(Gesture gesture) {
        switch (gesture) {
            case TAP:
                fragments.get(viewPager.getCurrentItem()).onSingleTapUp();
                return true;
            default:
                return super.onGesture(gesture);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }


    private SlidesApiTask.Listener createSlidesApiTaskListener() throws GeneralSecurityException, IOException {
        return new SlidesApiTask.Listener() {
            @Override
            public void onSuccess(List<Bitmap> thumbnails) {
                setContentView(R.layout.view_pager_layout);

                final ScreenSlidePagerAdapter screenSlidePagerAdapter = new ScreenSlidePagerAdapter(
                        getSupportFragmentManager());
                viewPager = findViewById(R.id.viewPager);
                viewPager.setAdapter(screenSlidePagerAdapter);

                for(int i = 0; i < thumbnails.size(); i++) {
                    fragments.add(ImageLayoutFragment
                            .newInstance(thumbnails.get(i)));
                }

                screenSlidePagerAdapter.notifyDataSetChanged();

                final TabLayout tabLayout = findViewById(R.id.page_indicator);
                tabLayout.setupWithViewPager(viewPager, true);
            }
        };
    }

    private GoogleCredentials loadServiceAccountCredential() throws IOException {
        Collection<String> scopes = new ArrayList<String>();
        scopes.add(SlidesScopes.PRESENTATIONS_READONLY);
        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open(SERVICE_ACCOUNT_FILENAME);
        GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
        return credentials;
    }
}

```