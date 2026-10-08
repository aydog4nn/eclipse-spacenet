import { useMemo, useRef } from 'react';
import { Canvas, useFrame, useThree } from '@react-three/fiber';
import { CanvasTexture, Color, MathUtils, SRGBColorSpace, Vector3, type Group, type Mesh } from 'three';
import type { SolarPlanet } from './solarSystemData';
import { solarPlanets } from './solarSystemData';

interface SolarSystemSceneProps {
  selectedPlanetId: string;
  onSelectPlanet: (planetId: string) => void;
}

const pseudoRandom = (seed: number) => {
  const value = Math.sin(seed * 91.345) * 47453.5453;
  return value - Math.floor(value);
};

const hexToRgb = (hex: string) => {
  const normalized = hex.replace('#', '');
  const value = Number.parseInt(normalized, 16);

  return {
    r: (value >> 16) & 255,
    g: (value >> 8) & 255,
    b: value & 255,
  };
};

const mixChannel = (base: number, tint: number, amount: number) => Math.round(base + (tint - base) * amount);

const getDisplayOrbitRadius = (distanceAu: number) => {
  // Preserve real orbital ordering and relative spread while fitting the scene on screen.
  return 5.5 + distanceAu * 2.8 + Math.sqrt(distanceAu) * 7.8;
};

const createPlanetTexture = (planet: SolarPlanet) => {
  const canvas = document.createElement('canvas');
  canvas.width = 256;
  canvas.height = 128;
  const context = canvas.getContext('2d');

  if (!context) {
    return null;
  }

  const baseRgb = hexToRgb(planet.color);
  const accentRgb = hexToRgb(planet.accent);
  const gradient = context.createLinearGradient(0, 0, canvas.width, canvas.height);

  gradient.addColorStop(0, `rgb(${mixChannel(baseRgb.r, 255, 0.16)} ${mixChannel(baseRgb.g, 255, 0.1)} ${mixChannel(baseRgb.b, 255, 0.08)})`);
  gradient.addColorStop(0.5, planet.color);
  gradient.addColorStop(1, `rgb(${mixChannel(baseRgb.r, 0, 0.22)} ${mixChannel(baseRgb.g, 0, 0.18)} ${mixChannel(baseRgb.b, 0, 0.14)})`);
  context.fillStyle = gradient;
  context.fillRect(0, 0, canvas.width, canvas.height);

  for (let band = 0; band < 20; band += 1) {
    const stripeY = pseudoRandom(band + planet.distanceAu) * canvas.height;
    const stripeHeight = 18 + pseudoRandom(band + planet.size * 10) * 74;
    const opacityBase = planet.surfaceType === 'gas' ? 0.12 : planet.surfaceType === 'cloudy' ? 0.08 : 0.05;
    const opacityRange = planet.surfaceType === 'gas' ? 0.18 : 0.12;
    const opacity = opacityBase + pseudoRandom(band + 41) * opacityRange;

    context.fillStyle = `rgba(${accentRgb.r}, ${accentRgb.g}, ${accentRgb.b}, ${opacity})`;
    context.fillRect(0, stripeY, canvas.width, stripeHeight);
  }

  for (let spot = 0; spot < 34; spot += 1) {
    const x = pseudoRandom(spot + 101 + planet.size) * canvas.width;
    const y = pseudoRandom(spot + 201 + planet.distanceAu) * canvas.height;
    const radius = 12 + pseudoRandom(spot + 301) * 68;
    const radial = context.createRadialGradient(x, y, 0, x, y, radius);

    radial.addColorStop(0, `rgba(${mixChannel(baseRgb.r, 255, 0.24)}, ${mixChannel(baseRgb.g, 255, 0.18)}, ${mixChannel(baseRgb.b, 255, 0.16)}, 0.18)`);
    radial.addColorStop(1, 'rgba(0, 0, 0, 0)');
    context.fillStyle = radial;
    context.beginPath();
    context.arc(x, y, radius, 0, Math.PI * 2);
    context.fill();
  }

  if (planet.surfaceType === 'rocky') {
    for (let crater = 0; crater < 24; crater += 1) {
      const x = pseudoRandom(crater + 401) * canvas.width;
      const y = pseudoRandom(crater + 501) * canvas.height;
      const radius = 6 + pseudoRandom(crater + 601) * 34;
      const craterGradient = context.createRadialGradient(x, y, radius * 0.15, x, y, radius);

      craterGradient.addColorStop(0, 'rgba(0, 0, 0, 0.18)');
      craterGradient.addColorStop(0.55, 'rgba(255, 255, 255, 0.05)');
      craterGradient.addColorStop(1, 'rgba(0, 0, 0, 0)');
      context.fillStyle = craterGradient;
      context.beginPath();
      context.arc(x, y, radius, 0, Math.PI * 2);
      context.fill();
    }
  }

  if (planet.surfaceType === 'cloudy' || planet.hasClouds) {
    for (let cloud = 0; cloud < 16; cloud += 1) {
      const cloudX = pseudoRandom(cloud + 701) * canvas.width;
      const cloudY = pseudoRandom(cloud + 801) * canvas.height;
      const cloudWidth = 80 + pseudoRandom(cloud + 901) * 260;
      const cloudHeight = 20 + pseudoRandom(cloud + 1001) * 72;
      const cloudGradient = context.createRadialGradient(
        cloudX,
        cloudY,
        cloudWidth * 0.08,
        cloudX,
        cloudY,
        cloudWidth
      );

      cloudGradient.addColorStop(0, 'rgba(255, 255, 255, 0.16)');
      cloudGradient.addColorStop(1, 'rgba(255, 255, 255, 0)');
      context.fillStyle = cloudGradient;
      context.beginPath();
      context.ellipse(cloudX, cloudY, cloudWidth, cloudHeight, pseudoRandom(cloud + 1101) * Math.PI, 0, Math.PI * 2);
      context.fill();
    }
  }

  if (planet.surfaceType === 'gas') {
    for (let storm = 0; storm < 8; storm += 1) {
      const x = pseudoRandom(storm + 1201) * canvas.width;
      const y = pseudoRandom(storm + 1301) * canvas.height;
      const radiusX = 26 + pseudoRandom(storm + 1401) * 120;
      const radiusY = 12 + pseudoRandom(storm + 1501) * 42;

      context.fillStyle = `rgba(${accentRgb.r}, ${accentRgb.g}, ${accentRgb.b}, 0.14)`;
      context.beginPath();
      context.ellipse(x, y, radiusX, radiusY, pseudoRandom(storm + 1601) * Math.PI, 0, Math.PI * 2);
      context.fill();
    }
  }

  const texture = new CanvasTexture(canvas);
  texture.colorSpace = SRGBColorSpace;
  texture.needsUpdate = true;
  return texture;
};

const createCloudTexture = (planet: SolarPlanet) => {
  const canvas = document.createElement('canvas');
  canvas.width = 256;
  canvas.height = 128;
  const context = canvas.getContext('2d');

  if (!context) {
    return null;
  }

  context.clearRect(0, 0, canvas.width, canvas.height);

  for (let cloud = 0; cloud < 22; cloud += 1) {
    const x = pseudoRandom(cloud + planet.distanceAu + 1700) * canvas.width;
    const y = pseudoRandom(cloud + planet.size + 1800) * canvas.height;
    const width = 50 + pseudoRandom(cloud + 1900) * 220;
    const height = 14 + pseudoRandom(cloud + 2000) * 60;
    const gradient = context.createRadialGradient(x, y, width * 0.04, x, y, width);

    gradient.addColorStop(0, 'rgba(255, 255, 255, 0.32)');
    gradient.addColorStop(0.55, 'rgba(255, 255, 255, 0.12)');
    gradient.addColorStop(1, 'rgba(255, 255, 255, 0)');
    context.fillStyle = gradient;
    context.beginPath();
    context.ellipse(x, y, width, height, pseudoRandom(cloud + 2100) * Math.PI, 0, Math.PI * 2);
    context.fill();
  }

  const texture = new CanvasTexture(canvas);
  texture.colorSpace = SRGBColorSpace;
  texture.needsUpdate = true;
  return texture;
};

const createSunTexture = () => {
  const canvas = document.createElement('canvas');
  canvas.width = 256;
  canvas.height = 256;
  const context = canvas.getContext('2d');

  if (!context) {
    return null;
  }

  const gradient = context.createRadialGradient(128, 128, 6, 128, 128, 128);
  gradient.addColorStop(0, '#fff4c9');
  gradient.addColorStop(0.22, '#ffd37d');
  gradient.addColorStop(0.55, '#ff964e');
  gradient.addColorStop(1, '#74291a');
  context.fillStyle = gradient;
  context.fillRect(0, 0, canvas.width, canvas.height);

  for (let flare = 0; flare < 36; flare += 1) {
    const angle = pseudoRandom(flare + 1) * Math.PI * 2;
    const distance = pseudoRandom(flare + 9) * 180;
    const x = 128 + Math.cos(angle) * distance * 0.5;
    const y = 128 + Math.sin(angle) * distance * 0.5;
    const radius = 12 + pseudoRandom(flare + 17) * 48;
    const radial = context.createRadialGradient(x, y, 0, x, y, radius);

    radial.addColorStop(0, 'rgba(255, 243, 179, 0.24)');
    radial.addColorStop(1, 'rgba(255, 120, 66, 0)');
    context.fillStyle = radial;
    context.beginPath();
    context.arc(x, y, radius, 0, Math.PI * 2);
    context.fill();
  }

  const texture = new CanvasTexture(canvas);
  texture.colorSpace = SRGBColorSpace;
  texture.needsUpdate = true;
  return texture;
};

const Starfield = ({
  count,
  minRadius,
  maxRadius,
  size,
  opacity,
}: {
  count: number;
  minRadius: number;
  maxRadius: number;
  size: number;
  opacity: number;
}) => {
  const [positions, colors] = useMemo(() => {
    const positionArray = new Float32Array(count * 3);
    const colorArray = new Float32Array(count * 3);
    const neutral = new Color('#ffffff');
    const soft = new Color('#f4f6fb');

    for (let index = 0; index < count; index += 1) {
      const i3 = index * 3;
      const radius = minRadius + pseudoRandom(index + 3) * (maxRadius - minRadius);
      const theta = pseudoRandom(index + 9) * Math.PI * 2;
      const phi = Math.acos(2 * pseudoRandom(index + 17) - 1);
      const tint = neutral.clone().lerp(soft, pseudoRandom(index + 29) * 0.55);

      positionArray[i3] = radius * Math.sin(phi) * Math.cos(theta);
      positionArray[i3 + 1] = radius * Math.cos(phi);
      positionArray[i3 + 2] = radius * Math.sin(phi) * Math.sin(theta);

      colorArray[i3] = tint.r;
      colorArray[i3 + 1] = tint.g;
      colorArray[i3 + 2] = tint.b;
    }

    return [positionArray, colorArray];
  }, [count, maxRadius, minRadius]);

  return (
    <points>
      <bufferGeometry>
        <bufferAttribute attach="attributes-position" args={[positions, 3]} />
        <bufferAttribute attach="attributes-color" args={[colors, 3]} />
      </bufferGeometry>
      <pointsMaterial size={size} vertexColors sizeAttenuation depthWrite={false} transparent opacity={opacity} />
    </points>
  );
};

const Sun = () => {
  const sunRef = useRef<Mesh>(null);
  const sunTexture = useMemo(() => createSunTexture(), []);

  useFrame((state) => {
    if (!sunRef.current) {
      return;
    }

    const elapsed = state.clock.getElapsedTime();
    sunRef.current.rotation.y = elapsed * 0.2;
  });

  return (
    <group>
      <mesh ref={sunRef}>
        <sphereGeometry args={[2.35, 36, 36]} />
        <meshStandardMaterial
          map={sunTexture}
          emissive="#ff8a4c"
          emissiveIntensity={2.4}
          color="#ffbf6d"
          roughness={0.38}
          metalness={0.08}
        />
      </mesh>
      <mesh scale={1.32}>
        <sphereGeometry args={[2.35, 18, 18]} />
        <meshBasicMaterial color="#ff8f4f" transparent opacity={0.12} />
      </mesh>
      <mesh scale={1.78}>
        <sphereGeometry args={[2.35, 18, 18]} />
        <meshBasicMaterial color="#ffb56d" transparent opacity={0.045} />
      </mesh>
    </group>
  );
};

const PlanetOrbit = ({
  planet,
  index,
  selectedPlanetId,
  onSelectPlanet,
  registerPlanetMesh,
}: {
  planet: SolarPlanet;
  index: number;
  selectedPlanetId: string;
  onSelectPlanet: (planetId: string) => void;
  registerPlanetMesh: (planetId: string, mesh: Mesh | null) => void;
}) => {
  const orbitRef = useRef<Group>(null);
  const planetRef = useRef<Mesh>(null);
  const ringRef = useRef<Mesh>(null);
  const cloudRef = useRef<Mesh>(null);
  const moonRefs = useRef<Array<Mesh | null>>([]);
  const isSelected = planet.id === selectedPlanetId;
  const displayOrbitRadius = getDisplayOrbitRadius(planet.distanceAu);
  const planetTexture = useMemo(() => createPlanetTexture(planet), [planet]);
  const cloudTexture = useMemo(() => (planet.hasClouds ? createCloudTexture(planet) : null), [planet]);
  const moons = useMemo(
    () =>
      Array.from({ length: planet.moons ?? 0 }, (_, moonIndex) => ({
        orbit: planet.size * (2.2 + moonIndex * 0.82),
        size: Math.max(0.08, planet.size * (0.16 - moonIndex * 0.025)),
        speed: 0.7 + moonIndex * 0.22,
        color: moonIndex % 2 === 0 ? '#d8d2c7' : '#b4a99b',
      })),
    [planet]
  );

  useFrame((state) => {
    const elapsed = state.clock.getElapsedTime();

    if (orbitRef.current) {
      orbitRef.current.rotation.y = elapsed * planet.speed * 0.8 + index * 0.5;
    }

    if (planetRef.current) {
      planetRef.current.rotation.y = elapsed * (0.42 + index * 0.06);
      planetRef.current.scale.setScalar(MathUtils.lerp(planetRef.current.scale.x, isSelected ? 1.14 : 1, 0.08));
    }

    if (cloudRef.current) {
      cloudRef.current.rotation.y = elapsed * (0.11 + index * 0.04);
    }

    if (ringRef.current) {
      ringRef.current.rotation.z = elapsed * (0.18 + index * 0.03);
    }

    moonRefs.current.forEach((moon, moonIndex) => {
      if (!moon) {
        return;
      }

      const moonData = moons[moonIndex];
      const moonAngle = elapsed * moonData.speed + moonIndex * 1.7;
      moon.position.x = Math.cos(moonAngle) * moonData.orbit;
      moon.position.z = Math.sin(moonAngle) * moonData.orbit;
    });
  });

  return (
    <group>
      <mesh rotation={[Math.PI / 2, 0, 0]}>
        <ringGeometry args={[displayOrbitRadius - 0.035, displayOrbitRadius + 0.035, 160]} />
        <meshBasicMaterial color="#ffffff" transparent opacity={0.16} side={2} />
      </mesh>

      <group ref={orbitRef}>
        <group position={[displayOrbitRadius, 0, 0]}>
          <mesh
            ref={(node) => {
              planetRef.current = node;
              registerPlanetMesh(planet.id, node);
            }}
            onClick={(event) => {
              event.stopPropagation();
              onSelectPlanet(planet.id);
            }}
          >
            <sphereGeometry args={[planet.size, 20, 20]} />
            <meshStandardMaterial
              map={planetTexture}
              color={planet.color}
              emissive={planet.emissive ?? planet.color}
              emissiveIntensity={planet.emissive ? 0.52 : 0.16}
              roughness={0.82}
              metalness={0.08}
            />
          </mesh>

          <mesh scale={1.12}>
            <sphereGeometry args={[planet.size, 14, 14]} />
            <meshBasicMaterial color={planet.accent} transparent opacity={isSelected ? 0.12 : 0.035} side={1} />
          </mesh>

          {planet.hasClouds && cloudTexture && (
            <mesh ref={cloudRef} scale={1.03}>
              <sphereGeometry args={[planet.size, 16, 16]} />
              <meshStandardMaterial map={cloudTexture} transparent opacity={planet.surfaceType === 'gas' ? 0.34 : 0.24} depthWrite={false} />
            </mesh>
          )}

          {planet.hasRings && (
            <mesh ref={ringRef} rotation={[Math.PI / 2.8, 0.4, 0]}>
              <torusGeometry args={[planet.size * 1.92, 0.12, 18, 180]} />
              <meshBasicMaterial color="#ead8bb" transparent opacity={0.28} />
            </mesh>
          )}

          {planet.hasRings && (
            <mesh rotation={[Math.PI / 2.8, 0.4, 0]}>
              <torusGeometry args={[planet.size * 2.16, 0.04, 14, 180]} />
              <meshBasicMaterial color="#f5ead8" transparent opacity={0.18} />
            </mesh>
          )}

          {moons.map((moon, moonIndex) => (
            <mesh
              key={`${planet.id}-moon-${moonIndex}`}
              ref={(node) => {
                moonRefs.current[moonIndex] = node;
              }}
              position={[moon.orbit, 0, 0]}
            >
              <sphereGeometry args={[moon.size, 12, 12]} />
              <meshStandardMaterial color={moon.color} roughness={0.9} metalness={0.03} />
            </mesh>
          ))}
        </group>
      </group>
    </group>
  );
};

const CameraRig = ({
  selectedPlanetId,
  planetGroupsRef,
}: {
  selectedPlanetId: string;
  planetGroupsRef: { current: Map<string, Mesh> };
}) => {
  const { camera } = useThree();
  const targetPositionRef = useRef(new Vector3());

  useFrame(() => {
    const targetGroup = planetGroupsRef.current.get(selectedPlanetId);
    const selectedPlanet = solarPlanets.find((planet) => planet.id === selectedPlanetId);
    const planetRadius = selectedPlanet?.size ?? 1;

    if (targetGroup) {
      targetGroup.getWorldPosition(targetPositionRef.current);
      const targetPosition = targetPositionRef.current;

      const desiredX = targetPosition.x + planetRadius * 5.4;
      const desiredY = Math.max(2.8, targetPosition.y + planetRadius * 1.8);
      const desiredZ = targetPosition.z + planetRadius * 8.2;

      camera.position.x = MathUtils.lerp(camera.position.x, desiredX, 0.04);
      camera.position.y = MathUtils.lerp(camera.position.y, desiredY, 0.04);
      camera.position.z = MathUtils.lerp(camera.position.z, desiredZ, 0.04);
      camera.lookAt(targetPosition.x, targetPosition.y, targetPosition.z);
      return;
    }

    camera.position.x = MathUtils.lerp(camera.position.x, 0, 0.03);
    camera.position.y = MathUtils.lerp(camera.position.y, 9, 0.03);
    camera.position.z = MathUtils.lerp(camera.position.z, 42, 0.03);
    camera.lookAt(0, 0, 0);
  });

  return null;
};

const SceneContent = ({ selectedPlanetId, onSelectPlanet }: SolarSystemSceneProps) => {
  const planetGroupsRef = useRef<Map<string, Mesh>>(new Map());
  const sceneGroupRef = useRef<Group>(null);

  const registerPlanetMesh = (planetId: string, mesh: Mesh | null) => {
    if (mesh) {
      planetGroupsRef.current.set(planetId, mesh);
    } else {
      planetGroupsRef.current.delete(planetId);
    }
  };

  useFrame((state) => {
    if (!sceneGroupRef.current) {
      return;
    }

    sceneGroupRef.current.rotation.y = Math.sin(state.clock.getElapsedTime() * 0.08) * 0.08;
  });

  return (
    <>
      <color attach="background" args={['#010205']} />
      <fog attach="fog" args={['#010205', 82, 260]} />
      <ambientLight intensity={0.14} />
      <pointLight position={[0, 0, 0]} intensity={158} color="#ffb06e" distance={145} decay={1.88} />
      <pointLight position={[0, 0, 0]} intensity={42} color="#fff4d1" distance={115} decay={2} />
      <directionalLight position={[12, 8, 14]} intensity={0.34} color="#ffffff" />
      <Starfield count={3600} minRadius={48} maxRadius={220} size={0.15} opacity={0.99} />
      <Starfield count={1100} minRadius={95} maxRadius={260} size={0.28} opacity={0.98} />
      <group ref={sceneGroupRef} rotation={[-0.16, 0, 0]}>
        <Sun />
        {solarPlanets.map((planet, index) => (
          <PlanetOrbit
            key={planet.id}
            planet={planet}
            index={index}
            selectedPlanetId={selectedPlanetId}
            onSelectPlanet={onSelectPlanet}
            registerPlanetMesh={registerPlanetMesh}
          />
        ))}
      </group>
      <CameraRig selectedPlanetId={selectedPlanetId} planetGroupsRef={planetGroupsRef} />
    </>
  );
};

const SolarSystemScene = ({ selectedPlanetId, onSelectPlanet }: SolarSystemSceneProps) => {
  return (
    <div className="solar-scene">
      <Canvas
        dpr={[0.75, 1]}
        gl={{ antialias: true, alpha: true, powerPreference: 'high-performance' }}
        camera={{ position: [0, 9, 42], fov: 36 }}
      >
        <SceneContent selectedPlanetId={selectedPlanetId} onSelectPlanet={onSelectPlanet} />
      </Canvas>
    </div>
  );
};

export default SolarSystemScene;
