package com.example.workerscontrol;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class AvatarUtils {

    private static final int AVATAR_SIZE_PX = 512;

    private AvatarUtils() {
    }

    public static void loadAvatar(ImageView imageView, String avatarPath) {
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.baseline_account_circle_24);
            return;
        }
        File file = new File(avatarPath);
        if (!file.exists()) {
            imageView.setImageResource(R.drawable.baseline_account_circle_24);
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            imageView.setImageResource(R.drawable.baseline_account_circle_24);
            return;
        }
        imageView.setImageBitmap(bitmap);
    }

    public static String saveRoundedAvatar(Context context, Uri sourceUri, long workerId) throws IOException {
        Bitmap original = decodeSampledBitmap(context.getContentResolver(), sourceUri, AVATAR_SIZE_PX, AVATAR_SIZE_PX);
        if (original == null) {
            throw new IOException("Не удалось прочитать изображение");
        }

        Bitmap square = cropCenterSquare(original);
        Bitmap resized = Bitmap.createScaledBitmap(square, AVATAR_SIZE_PX, AVATAR_SIZE_PX, true);
        Bitmap rounded = toRoundBitmap(resized);

        File avatarDir = new File(context.getFilesDir(), "avatars");
        if (!avatarDir.exists() && !avatarDir.mkdirs()) {
            throw new IOException("Не удалось создать директорию для аватарок");
        }

        File avatarFile = new File(avatarDir, "worker_" + workerId + "_avatar.png");
        try (FileOutputStream outputStream = new FileOutputStream(avatarFile)) {
            if (!rounded.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                throw new IOException("Не удалось сохранить аватарку");
            }
            outputStream.flush();
        }
        return avatarFile.getAbsolutePath();
    }

    private static Bitmap cropCenterSquare(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        return Bitmap.createBitmap(source, x, y, size, size);
    }

    private static Bitmap toRoundBitmap(Bitmap source) {
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        float radius = source.getWidth() / 2f;
        canvas.drawCircle(radius, radius, radius, paint);
        return output;
    }

    private static Bitmap decodeSampledBitmap(ContentResolver resolver, Uri uri, int reqWidth, int reqHeight) throws IOException {
        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) {
                return null;
            }
            BitmapFactory.decodeStream(input, null, boundsOptions);
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight);
        decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) {
                return null;
            }
            return BitmapFactory.decodeStream(input, null, decodeOptions);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
