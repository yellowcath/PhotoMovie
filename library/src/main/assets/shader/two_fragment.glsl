varying highp vec2 textureCoordinate;
varying highp vec2 textureCoordinate2;
uniform sampler2D inputImageTexture;
uniform sampler2D inputImageTexture2;
void main()
{
     vec4 texColor = texture2D(inputImageTexture, textureCoordinate);
     vec4 texColor2 = texture2D(inputImageTexture2, textureCoordinate2);
     gl_FragColor = texColor*texColor2;
}