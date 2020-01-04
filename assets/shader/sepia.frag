#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

// vignette effect variables
uniform vec2 resolution;
uniform float radius;
// vignette effect constants
const float SOFTNESS = 0.25;
const float VIGNETTE_OPACITY = 0.95;

// sepia effect constants
const vec3 grayScaleMultiplier = vec3(0.299, 0.587, 0.114);
const vec3 sepiaMultiplier = vec3(1.2, 1.0, 0.8);

void main()
{
    // calc relative position of pixel on screen from center
    // if we remove "-vec2(0.5)" then the vignette circle starts from bottom left (0,0) instead of center
    vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);
    // fix aspect ratio discrepancy
    position.x *= resolution.x / resolution.y;
    float distToCenter = length(position);
    // vignetteRadius value is between 0 and 1 where 1 is the edge of the screen and 0 is the center
    float vignetteRadius = smoothstep(radius, radius+SOFTNESS, 1.0 - distToCenter);

    // calculater color
    // 1) normal sprite color with vignette
    vec4 spriteColor = v_color * texture2D(u_texture, v_texCoords);
    spriteColor.rgb = mix(spriteColor.rgb, spriteColor.rgb*vignetteRadius, VIGNETTE_OPACITY);
    // 2) apply sepia effect
    vec3 grayColor = vec3(dot(spriteColor.rgb, grayScaleMultiplier));
    gl_FragColor = vec4(grayColor * sepiaMultiplier, spriteColor.a);
}
