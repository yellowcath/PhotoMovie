[中文简介](https://blog.csdn.net/yellowcath/article/details/82664987)

[PhotoMovie](https://github.com/yellowcath/PhotoMovie) can easily achieve the function of PhotoMovie like TikTok.
The functions are shown below.

### Filter

![image](https://github.com/yellowcath/PhotoMovie/raw/master/readme/filter.gif)

### Transition

![image](https://github.com/yellowcath/PhotoMovie/raw/master/readme/transfer.gif)

## Gradle

add Maven
``` groovy
 allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
```
add implementation
``` groovy
dependencies {
    implementation 'com.github.yellowcath:PhotoMovie:1.6.3'
}
```
### Changelog
#### 1.6.0
* Synchronous remix while recording video  
>Recording PhotoMovie with Music  
Before:------------Record Video------------>------Mix Music------>End  
Now:   --------Record Video And Music------>End  

#### 1.6.1
* Support to add watermark
``` java
GLMovieRenderer.setWaterMark(bitmap,dstRect,alpha);
GLMovieRenderer.setWaterMark(text,textSize,textColor,x,y);
```

#### 1.6.2
* Fixed memory leak when switching transfer animation in demo

#### 1.6.3
* Fixed crash of encoder "OMX.MTK.VIDEO.ENCODER.AVC"
### Usage
For reference [DemoPresenter](https://github.com/yellowcath/PhotoMovie/blob/master/app/src/main/java/com/hw/photomovie/sample/DemoPresenter.java)
``` java
        //add photo
        List<PhotoData> photoDataList = new LinkedList<PhotoData>();
        photoDataList.add(new SimplePhotoData(context,photoPath1,PhotoData.STATE_LOCAL));
        ...
        photoDataList.add(new SimplePhotoData(context,photoPathN,PhotoData.STATE_LOCAL));

        PhotoSource photoSource = new PhotoSource(photoDataList);
        //generate PhotoMoive
        PhotoMovie photoMovie = PhotoMovieFactory.generatePhotoMovie(photoSource, PhotoMovieFactory.PhotoMovieType.HORIZONTAL_TRANS);
        //generate MovieRenderer for rendering PhotoMovie to glTextureView
        MovieRenderer movieRenderer = new GLTextureMovieRender(glTextureView);
        /**
         * OR  MovieRenderer movieRenderer = new GLSurfaceMovieRenderer(glSurfaceView);
         */
        //PhotoMoviePlayer
        PhotoMoviePlayer photoMoviePlayer = new PhotoMoviePlayer(context);
        photoMoviePlayer.setMovieRenderer(mMovieRenderer);
        photoMoviePlayer.setMovieListener(...);
        photoMoviePlayer.setLoop(true);
        photoMoviePlayer.setOnPreparedListener(new PhotoMoviePlayer.OnPreparedListener() {
            @Override
            public void onPreparing(PhotoMoviePlayer moviePlayer, float progress) {
            }

            @Override
            public void onPrepared(PhotoMoviePlayer moviePlayer, int prepared, int total) {
                 mPhotoMoviePlayer.start();
            }

            @Override
            public void onError(PhotoMoviePlayer moviePlayer) {
            }
        });
        photoMoviePlayer.prepare();
```

### Extend the functionality
PhotoMovie uses a modular design, and each part can be customized and replaced.

Class Diagram
![image](https://github.com/yellowcath/PhotoMovie/raw/master/readme/PhotoMovie.png)

- **MovieSegment**:Each MovieSegment has a specific length of time during which images are played in a specific way.
For example
>ScaleSegment will do scale animation.
>EndGaussianBlurSegment will do a gaussian blur animation from clear to blurry
- **PhotoMovie**:PhotoMovie stands for the Movie itself，A complete PhotoMovie consists of a PhotoSource and a series of MovieSegment，images are allocated to MovieSegment through PhotoAllocator.

- **MovieLayer**:MovieLayer provides the ability to render multilayer effects for MovieSegment ， such as SubtitleLayer with subtitle demonstrations

- **IMovieFilter**:MovieFilter provides filters for the entire PhotoMovie.

- **MovieRenderer**:The MovieRenderer's function is to render the photo movie to the specified output interface, such as TextureView (GLTextureMovieRender), GLSurfaceView(GLSurfaceMovieRenderer)

- **PhotoMoviePlayer**:PhotoMoviePlayer provides a MediaPlayer-like interface to play PhotoMovie，its progress is controlled by IMovieTimer.


#### Add PhotoMovie Type

Six types are currently built in
``` java
 public enum PhotoMovieType {
        THAW,
        SCALE,
        SCALE_TRANS,
        WINDOW,
        HORIZONTAL_TRANS,
        VERTICAL_TRANS
    }
```

Here is an example of how to extend the gradient effect of 微视

According to the analysis, the picture was placed in the center first, and then a weak zoom in animation was made throughout, and the change of transparency disappeared in the second half
，A more intuitive flow chart is shown below
![image](https://github.com/yellowcath/PhotoMovie/raw/master/readme/gradient_timeline.png)

So two different MovieSegment is required.

FitCenterScaleSegment for zooming in images
``` java
public class FitCenterScaleSegment extends FitCenterSegment {
    /**
     * scale range
     */
    private float mScaleFrom;
    private float mScaleTo;

    private float mProgress;

    /**
     * @param duration
     * @param scaleFrom
     * @param scaleTo
     */
    public FitCenterScaleSegment(int duration, float scaleFrom, float scaleTo) {
        super(duration);
        mScaleFrom = scaleFrom;
        mScaleTo = scaleTo;
    }

    @Override
    protected void onDataPrepared() {
        super.onDataPrepared();
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        mProgress = segmentProgress;
        if (!mDataPrepared) {
            return;
        }
        drawBackground(canvas);
        float scale = mScaleFrom + (mScaleTo - mScaleFrom) * mProgress;
        drawContent(canvas, scale);
    }
        @Override
    public void drawContent(GLESCanvas canvas, float scale) {
        super.drawContent(canvas, scale);
    }

    @Override
    public void drawBackground(GLESCanvas canvas) {
        super.drawBackground(canvas);
    }
}
```
And then create the GradientTransferSegment,it's superclass contains both the prior and the next MovieSegment for implementing any transition animation.
``` java
public class GradientTransferSegment extends TransitionSegment<FitCenterScaleSegment, FitCenterScaleSegment> {

    private float mPreScaleFrom;
    private float mPreScaleTo;
    private float mNextScaleFrom;
    private float mNextScaleTo;

    public GradientTransferSegment(int duration,
                                   float preScaleFrom, float preScaleTo,
                                   float nextScaleFrom, float nextScaleTo) {
        mPreScaleFrom = preScaleFrom;
        mPreScaleTo = preScaleTo;
        mNextScaleFrom = nextScaleFrom;
        mNextScaleTo = nextScaleTo;
        setDuration(duration);
    }

    @Override
    protected void onDataPrepared() {

    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        //zoom in animation
        float nextScale = mNextScaleFrom + (mNextScaleTo - mNextScaleFrom) * segmentProgress;
        mNextSegment.drawContent(canvas, nextScale);

        //zoom out & alpha animation
        float preScale = mPreScaleFrom + (mPreScaleTo - mPreScaleFrom) * segmentProgress;
        float alpha = 1 - segmentProgress;
        mPreSegment.drawBackground(canvas);
        canvas.save();
        canvas.setAlpha(alpha);
        mPreSegment.drawContent(canvas, preScale);
        canvas.restore();
    }
```
Generate PhotoMovie Object
``` java
    private static PhotoMovie initGradientPhotoMovie(PhotoSource photoSource) {
        List<MovieSegment> segmentList = new ArrayList<>(photoSource.size());
        for (int i = 0; i < photoSource.size(); i++) {
            if (i == 0) {
                segmentList.add(new FitCenterScaleSegment(1600, 1f, 1.1f));
            } else {
                segmentList.add(new FitCenterScaleSegment(1600, 1.05f, 1.1f));
            }
            if (i < photoSource.size() - 1) {
                segmentList.add(new GradientTransferSegment(800, 1.1f, 1.15f, 1.0f, 1.05f));
            }
        }
        return new PhotoMovie(photoSource, segmentList);
    }
```
Then you can play this PhotoMovie like this:

![image](https://github.com/yellowcath/PhotoMovie/raw/master/readme/gradient.gif)

#### Add new filter
Nine filters are currently built in
``` java
public enum  FilterType {
    NONE,
    CAMEO,
    GRAY,
    KUWAHARA,
    SNOW,//dynamic filter
    LUT1,
    LUT2,
    LUT3,
    LUT4,
    LUT5,
}
```
IMovieFilter
``` java
public interface IMovieFilter {
    void doFilter(PhotoMovie photoMovie,int elapsedTime, FboTexture inputTexture, FboTexture outputTexture);
    void release();
}
```
The MovieRenderer will provide an input texture that is then drawn to the output texture after being processed by the IMovieFilter, which implements the filter effect.

BaseMovieFilter has implemented basic input and output processes.
For example, to make a basic black and white filter, just change FRAGMENT_SHADER
``` java
public class GrayMovieFilter extends BaseMovieFilter {
    protected static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     mediump vec4 color = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     mediump float gray = color.r*0.3+color.g*0.59+color.b*0.11;\n"+
            "     gl_FragColor = vec4(gray,gray,gray,1.0);\n"+
            "}";
    public GrayMovieFilter(){
        super(VERTEX_SHADER,FRAGMENT_SHADER);
    }
}
```
PhotoMovie also offers support for Lut filters
>Lut means [Lookup Table](https://en.wikipedia.org/wiki/Lookup_table)

For example

lut | filter
---|---
<img src="https://github.com/yellowcath/PhotoMovie/raw/master/library/src/main/assets/lut/lut_default.png" width="200" hegiht="200" align=center /> | <img src="https://github.com/yellowcath/PhotoMovie/raw/master/app/src/main/res/drawable-nodpi//filter_default.jpg" width="200" hegiht="200" align=center />
<img src="https://github.com/yellowcath/PhotoMovie/raw/master/library/src/main/assets/lut/lut_2.jpg" width="200" hegiht="200" align=center /> | <img src="https://github.com/yellowcath/PhotoMovie/raw/master/app/src/main/res/drawable-nodpi/l2.jpg" width="200" hegiht="200" align=center />


``` java
public class LutMovieFilter extends TwoTextureMovieFilter {

    public LutMovieFilter(Bitmap lutBitmap){
        super(loadShaderFromAssets("shader/two_vertex.glsl"),loadShaderFromAssets("shader/lut.glsl"));
        setBitmap(lutBitmap);
    }
}
```

### Record
[GLMovieRecorder](https://github.com/yellowcath/PhotoMovie/blob/master/library/src/main/java/record/GLMovieRecorder.java) provides the ability to record photo movies as mp4

See [DemoPresenter](https://github.com/yellowcath/PhotoMovie/blob/master/app/src/main/java/com/hw/photomovie/sample/DemoPresenter.java) saveVideo()
``` java
        GLMovieRecorder recorder = new GLMovieRecorder();
        recorder.configOutput(width, height(), bitrate,frameRate,iFrameInterval, outputPath);
        recorder.setDataSource(movieRenderer);
        recorder.startRecord(new GLMovieRecorder.OnRecordListener() {
            @Override
            public void onRecordFinish(boolean success) {
               ......
            }

            @Override
            public void onRecordProgress(int recordedDuration, int totalDuration) {
               ......
            }
        });
```
### Background Music

#### Play
``` java
 mPhotoMoviePlayer.setMusic(context, mMusicUri);
```

#### Record
``` java
 glMovieRecorder.setMusic(audioPath);
```



