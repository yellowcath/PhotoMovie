precision mediump float;
varying highp vec2 textureCoordinate;
uniform sampler2D inputImageTexture;

const float PI  = 3.14159265359;
const float aoinParam1 = 0.7;

uniform float time;
uniform vec2 resolution;

float snow(vec2 uv,float scale)
{
	float w=smoothstep(9.,0.,-uv.y*(scale/10.));
	if(w<.1)return 0.;
	uv+=(time*aoinParam1)/scale;
	uv.y+=time*0./scale;
	uv.x+=sin(uv.y+time*.05)/scale;
	uv*=scale;
	vec2 s=floor(uv),f=fract(uv),p;
	float k=3.,d;
	p=.5+.35*sin(11.*fract(sin((s+p+scale)*mat2(7,3,6,5))*5.))-f;
	d=length(p);
	k=min(d,k);
	k=smoothstep(0.,k,sin(f.x+f.y)*0.01);
    	return k*w;
}
void main(void)
{
    vec4 texColor = texture2D(inputImageTexture, textureCoordinate);

    float T = (time / .99);
    vec2 position = (( textureCoordinate.xy / resolution.xy ) - 0.5);
    position.x *= resolution.x / resolution.y;
    vec3 color = texColor.rgb;
    vec2 uv=(textureCoordinate.xy*2.-resolution.xy)/min(resolution.x,resolution.y);
    vec3 finalColor=vec3(0);
    float c=0.0;
	c+=snow(uv,30.)*.3;
	c+=snow(uv,8.);
	c+=snow(uv,6.);
	c+=snow(uv,5.);
	finalColor=(vec3(c));

    gl_FragColor = (vec4( color, 1.0 ) + vec4(finalColor,1));
}