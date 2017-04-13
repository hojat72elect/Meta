package de.fatox.meta.api.graphics;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.fatox.meta.api.dao.GLShaderData;
import de.fatox.meta.api.dao.RenderTargetData;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Frotty on 02.07.2016.
 */
public class GLShaderHandle {
    public GLShaderData data;
    public Array<RenderTargetData> targets = new Array<>();
    private FileHandle vertexHandle;
    private FileHandle fragmentHandle;

    private static final Pattern outPattern = Pattern.compile("(.*)(out)(\\s)+(vec[2-4])(\\s)+(\\w+)(;)");

    public GLShaderHandle(String name, FileHandle vertexHandle, FileHandle fragmentHandle) {
        this.data = new GLShaderData(name, vertexHandle.path(), fragmentHandle.path());
        this.vertexHandle = vertexHandle;
        this.fragmentHandle = fragmentHandle;
        fetchRendertargets();
    }

    public GLShaderHandle(GLShaderData data, FileHandle vertexHandle, FileHandle fragmentHandle) {
        this.data = data;
        this.vertexHandle = vertexHandle;
        this.fragmentHandle = fragmentHandle;
        fetchRendertargets();
    }

    public FileHandle getVertexHandle() {
        return vertexHandle;
    }

    public void setVertexHandle(FileHandle vertexHandle) {
        this.vertexHandle = vertexHandle;
        data.vertexFilePath = vertexHandle.path();
    }

    public FileHandle getFragmentHandle() {
        return fragmentHandle;
    }

    public void setFragmentHandle(FileHandle fragmentHandle) {
        this.fragmentHandle = fragmentHandle;
        data.fragmentFilePath = fragmentHandle.path();
    }

    private void fetchRendertargets() {
        targets.clear();
        try(BufferedReader br = new BufferedReader(fragmentHandle.reader())) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.startsWith("layout")) {
                    Matcher matcher = outPattern.matcher(line);
                    if(matcher.matches()) {
                        String type = matcher.group(4);
                        String name = matcher.group(6);
                        targets.add(new RenderTargetData(type, name));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
