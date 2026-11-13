package com.example.ateli;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Prepara a foto antes do envio: reduz o tamanho (foto de câmera tem
 * 5–15 MB) e corrige a rotação (muitos celulares salvam a foto "deitada").
 */
public final class Fotos {

    private static final int LADO_MAXIMO = 1600; // pixels
    private static final int QUALIDADE_JPEG = 85;

    private Fotos() { }

    /** Cria um arquivo vazio no cache para a câmera gravar a foto. */
    public static File novoArquivoCamera(Context contexto) throws IOException {
        File pasta = new File(contexto.getCacheDir(), "fotos");
        if (!pasta.exists() && !pasta.mkdirs()) {
            throw new IOException("Não foi possível criar a pasta de fotos");
        }
        return File.createTempFile("camera_", ".jpg", pasta);
    }

    /** Lê a foto da Uri e devolve um JPEG reduzido e na rotação certa. Rodar fora da thread principal. */
    public static File comprimir(Context contexto, Uri uri) throws IOException {
        // 1) Descobre o tamanho original sem carregar a imagem inteira na memória
        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inJustDecodeBounds = true;
        try (InputStream in = contexto.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, opcoes);
        }
        if (opcoes.outWidth <= 0 || opcoes.outHeight <= 0) {
            throw new IOException("Arquivo não é uma imagem");
        }

        // 2) Carrega já reduzida (potência de 2)
        int amostra = 1;
        while (Math.max(opcoes.outWidth, opcoes.outHeight) / (amostra * 2) >= LADO_MAXIMO) {
            amostra *= 2;
        }
        opcoes = new BitmapFactory.Options();
        opcoes.inSampleSize = amostra;
        Bitmap bitmap;
        try (InputStream in = contexto.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, opcoes);
        }
        if (bitmap == null) {
            throw new IOException("Não foi possível ler a foto");
        }

        // 3) Ajuste fino do tamanho + rotação indicada no EXIF da foto
        Matrix matriz = new Matrix();
        float escala = Math.min(1f, (float) LADO_MAXIMO / Math.max(bitmap.getWidth(), bitmap.getHeight()));
        matriz.postScale(escala, escala);
        matriz.postRotate(lerRotacao(contexto, uri));
        Bitmap ajustada = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matriz, true);
        if (ajustada != bitmap) {
            bitmap.recycle();
        }

        // 4) Salva como JPEG no cache
        File destino = new File(contexto.getCacheDir(), "fotos/envio_" + System.currentTimeMillis() + ".jpg");
        File pasta = destino.getParentFile();
        if (pasta != null && !pasta.exists()) {
            pasta.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(destino)) {
            ajustada.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, out);
        } finally {
            ajustada.recycle();
        }
        return destino;
    }

    private static int lerRotacao(Context contexto, Uri uri) {
        try (InputStream in = contexto.getContentResolver().openInputStream(uri)) {
            if (in == null) return 0;
            int orientacao = new ExifInterface(in).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientacao) {
                case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }
}
