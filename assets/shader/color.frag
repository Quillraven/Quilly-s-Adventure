#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float grayness;

const vec3 grayScaleMultiplier = vec3(0.299, 0.587, 0.114);

void main()
{
    vec4 spriteColor = v_color * texture2D(u_texture, v_texCoords);
    vec3 grayColor = vec3(dot(spriteColor.rgb, grayScaleMultiplier));
    vec3 blendedColor = mix(spriteColor.rgb, grayColor, grayness);
    gl_FragColor = vec4(blendedColor.rgb, spriteColor.a);
}
