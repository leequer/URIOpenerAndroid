package com.github.webee.uriopener.core;

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;

import com.github.webee.uriopener.openctxprocessors.UseAppContextOpenCtxProcessor;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by webee on 17/2/17.
 */

public final class URIOpeners {
    private static Application app;
    public static final List<OpenContextProcessor> openCtxProcessors = new LinkedList<>();
    public static final List<Opener> openers = new LinkedList<>();

    public static void init(Application app) {
        URIOpeners.app = app;
        addContextProcessor(new UseAppContextOpenCtxProcessor(app));
    }

    public static void addContextProcessor(OpenContextProcessor ...processors) {
        for (int i = 0; i < processors.length; i++) {
            openCtxProcessors.add(processors[i]);
        }
    }

    public static void insertOpener(Opener... newOpeners) {
        for (int i = newOpeners.length - 1; i >= 0; i--) {
            openers.add(0, newOpeners[i]);
        }
    }

    public static void appendOpener(Opener... newOpeners) {
        for (int i = 0; i < newOpeners.length; i++) {
            openers.add(newOpeners[i]);
        }
    }

    public static boolean open(String path) {
        return open(app, path);
    }

    public static boolean open(Uri uri) {
        return open(app, uri);
    }

    public static boolean open(android.content.Context context, String path) {
        return open(context, Uri.parse(path));
    }

    public static boolean open(android.content.Context context, Uri uri) {
        return open(context, uri, null, null);
    }

    public static boolean open(android.content.Context context, String path, Data ctxData) {
        return open(context, Uri.parse(path), ctxData, null);
    }

    public static boolean open(android.content.Context context, String path, Data ctxData, Bundle reqData) {
        return open(context, Uri.parse(path), ctxData, reqData);
    }

    public static boolean open(android.content.Context context, Uri uri, Data ctxData, Bundle reqData) {
        return open(new OpenContext(context, uri, ctxData, reqData));
    }

    public static boolean open(OpenContext ctx) {
        // 1. 处理open context
        for (OpenContextProcessor processor : openCtxProcessors) {
            ctx = processor.process(ctx);
        }

        // 2. 尝试使用打开器处理请求
        for (Opener opener : openers) {
            try {
                if (opener.open(ctx)) {
                    return true;
                }
            } catch (Throwable r) {
                r.printStackTrace();
            }
        }
        return false;
    }

    public static OpenContextBuilder route(String path) {
        return route(Uri.parse(path));
    }

    public static OpenContextBuilder route(Uri uri) {
        return new OpenContextBuilder(uri);
    }

    public static class OpenContextBuilder {
        private android.content.Context context;
        private Uri uri;
        private Data ctxData;
        private Bundle reqData;

        OpenContextBuilder(Uri uri) {
            this.uri = uri;
        }

        public OpenContextBuilder withContext(android.content.Context context) {
            this.context = context;
            return this;
        }

        public OpenContextBuilder withCtxData(Data ctxData) {
            if (ctxData != null) {
                if (this.ctxData == null) {
                    this.ctxData = new Data();
                }
                this.ctxData.putAll(ctxData);
            }
            return this;
        }

        public OpenContextBuilder withReqData(Bundle reqData) {
            if (reqData != null) {
                if (this.reqData == null) {
                    this.reqData = new Bundle();
                }
                this.reqData.putAll(reqData);
            }
            return this;
        }

        public boolean open() {
            return URIOpeners.open(new OpenContext(context, uri, ctxData, reqData));
        }
    }
}
