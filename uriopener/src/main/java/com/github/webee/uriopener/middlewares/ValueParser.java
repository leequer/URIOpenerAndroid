package com.github.webee.uriopener.middlewares;

import android.os.Bundle;

/**
 * Created by webee on 17/2/22.
 */

public interface ValueParser {
    void parse(Bundle data, String name, String value);
}
