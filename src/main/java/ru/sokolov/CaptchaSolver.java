package ru.sokolov;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class CaptchaSolver {

    private ComputationGraph model = null;


    public CaptchaSolver() throws IOException {
        InputStream is = CaptchaSolver.class.getResourceAsStream("/model.zip");
        model = ModelSerializer.restoreComputationGraph(is);
    }

    public CaptchaSolver(String modelPath) throws IOException {
        File f = new File(modelPath);
        if (f.exists()) {
            model = ModelSerializer.restoreComputationGraph(modelPath);
        } else {
            throw new IOException("No model found in " + modelPath);
        }
    }

    protected void finalize() {}

    public String solve(File captchaPath) throws IOException {
        List<String> labelList =
                Arrays.asList(
                        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

        INDArray image = loadImage(captchaPath);
        INDArray[] output = model.output(image);

        String captcha = "";
        for (int digit = 0; digit < 5; digit++) {
            INDArray poutput = output[digit].getRow(0);
            int index = Nd4j.argMax(poutput, 1).getInt(0);
            captcha += labelList.get(index);
        }

        return captcha;
    }

    private INDArray loadImage(File path) throws IOException {
        int height = 60;
        int width = 200;
        int channels = 1;

        NativeImageLoader loader = new NativeImageLoader(height, width, channels);
        INDArray image = loader.asMatrix(path);

        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.transform(image);

        return image;
    }
}
