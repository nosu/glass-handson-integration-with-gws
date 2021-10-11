id: glass_integration_with_gws
status: [draft]
author: Wataru Inoue
summary: Google Workspace と連携したアプリの開発
categories: Android, Glass
tags: android, googleglass, gws, workspace
feedback link: TBE

# Google Workspace と連携したアプリの開発

## 概要

<!-- https://developer.android.com/codelabs/advanced-android-training-fragments?hl=en&continue=https%3A%2F%2Fcodelabs.developers.google.com%2F#0 -->

### はじめに

このコースでは、Google Glass Enterprise Edition 2 向けに、Google Workspace (以下 `GWS`) と連携するアプリケーションの開発を行います。
Glass 用のアプリケーションは、オフラインで完結するものを開発することも可能ですが、GWS に含まれる Google Drive / Google Calendar / Google Slides 等の様々なクラウドサービスと連携することで、より便利な機能を実現することができます。

今回は、Google Slides と連携して、PC 等で作成したスライド（プレゼンテーション）を、Glass の画面に表示し、スワイプ操作でページめくりを行うことができるアプリケーションを開発してみます。

![プロジェクト選択](img/app_capture.gif)

### 必要な事前知識

このコースでは、以下の事前知識を前提としています。
- Android Studio の基本操作
- Google Glass Enterprise Edition 2 (またはエミュレータ)の操作方法
- Android アプリケーション開発の基礎

### このコースで学ぶこと

- Google API Client の使い方
- Fragment や Layout の作成方法
- Android における非同期処理の実装方法

### このコースでやること

`CardSample` プロジェクトをベースに、Google Slides と連携して、PC 等で作成したスライド（プレゼンテーション）を、Glass の画面に表示し、スワイプ操作でページめくりを行うことができるアプリケーションを開発してみます。

## ハンズオンの概要

前半では、Slides API をはじめとするクラウド側の準備作業を行います。
後半では、 `CardSample` をベースとして、スライドを表示するための実装を行います。

### 実装するアプリのイメージ

## Google Cloud / GWS 側の準備

まずは、アプリから Slides API を呼び出すことができるように、クラウドサービス側の準備を行います。

- Google Cloud プロジェクトの作成・選択
- Slides API の有効化
- サービスアカウントの作成と権限の付与
- サービスアカウントの鍵を作成・ダウンロード
- テスト用スライドの準備

### Google Cloud プロジェクトの作成・選択

今日のハンズオン用に Google Cloud のプロジェクトを作成します。

- Cloud Console ([https://console.cloud.google.com](https://console.cloud.google.com)) にアクセスする
- ヘッダにあるプロジェクト名をクリックし、「新しいプロジェクト」を選択
![ヘッダ](img/console_header.png)
<!-- ![プロジェクト選択](img/select_project.png) -->
- 適当なプロジェクト名を入力し、「作成」をクリック
![プロジェクト選択](img/create_new_project.png)
- 作成完了の通知が表示されたら、通知ポップアップから作成したプロジェクトに切り替える

> aside positive
> または、作成済みのプロジェクトをそのまま利用いただいても構いません。その場合は、ヘッダにあるプロジェクト名をクリックしてから、使用したいプロジェクトを選択してください。

### Slides API の有効化

Google Cloud では、意図しない API の利用を防ぐために、デフォルトではすべての API が無効化されています。
今回開発するアプリでは Slides API を利用したいので、Slides API を明示的に有効化します。

- Console 画面上部にある検索窓に、”slides api” などと入力し、表示された候補から「Google Slides API」を選択する
- 「有効にする」をクリックしてしばらく待つ

### サービスアカウントの作成と権限の付与

- Console の左メニューから「IAM と管理」→「サービスアカウント」をクリック
- 「＋サービスアカウントを作成」をクリック

### サービスアカウントの鍵を作成・ダウンロード




### テスト用スライドの準備

アプリから表示してみるためのテスト用スライドを用意します。

- [Google Slides](https://docs.google.com/presentation/u/0/?tgif=d) にアクセスし、新規スライドを作成する
Positive:
ブラウザから [slides.new](https://slides.new/) にアクセスすることでも新しいスライドを作成できます
- 作成したスライドに新しいページを何枚か追加し、適当な内容を記載する

### サービスアカウントにテスト用スライドへの閲覧権限を付与



## アプリ実装(1)

### ベースとなるプロジェクトの準備

今回は、公式の Glass 用サンプルアプリ集にある `CardSample` というアプリをベースに実装を行います。
リポジトリをクローンし、Android Studio で開いてみましょう。

- 以下のコマンドで Git リポジトリをローカルに Clone する
```bash
git clone https://github.com/googlesamples/glass-enterprise-samples.git
```
- Android Studio で File -> Open をクリックし、 `glass-enterprise-samples/CardSample` を選択して開く


### Google API Client ライブラリの追加

Android Studio のビルドツールである Gradle の設定ファイル（build.gradle）に、Slides API 用ライブラリと認証用ライブラリを依存関係として追記する

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


#### Google API Client ライブラリについて

TODO: 説明を記載


### PERMISSION の追加


## アプリ実装(2) Fragment の実装

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

 （補足）Activity / Fragment / View のイメージ



## アプリ実装(3) API からデータを取得する非同期処理の実装

Google API Client を使い、Slides API からデータを取得するための処理を記述していきます。
Android アプリでは、HTTP 通信を伴う時間のかかる処理は、メインスレッド（UI スレッド）ではなく、ワーカースレッドで非同期に実行する必要があります。
今回は、非同期処理を実装する方法として `AsyncTask` class を利用します。

### AsyncTask を継承した

メインスレッドから AsyncTask を呼び出す（`execute()`) すると、

do

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

### MainActivity の実装

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