import { useMemo, useRef } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Color, type Group, type Mesh, type Points as ThreePoints } from 'three';

const pseudoRandom = (seed: number) => {
  const value = Math.sin(seed * 12.9898) * 43758.5453123;
  return value - Math.floor(value);
};

const ParticleGalaxy = () => {
  const groupRef = useRef<Group>(null);
  const pointsRef = useRef<ThreePoints>(null);

  const [positions, colors] = useMemo(() => {
    const count = 5000;
    const armCount = 5;
    const radius = 5.8;
    const positionArray = new Float32Array(count * 3);
    const colorArray = new Float32Array(count * 3);
    const innerColor = new Color('#fff3d1');
    const midColor = new Color('#ff7a5c');
    const outerColor = new Color('#5aa6ff');

    for (let index = 0; index < count; index += 1) {
      const i3 = index * 3;
      const distance = pseudoRandom(index + 1) * radius;
      const branchAngle = ((index % armCount) / armCount) * Math.PI * 2;
      const spinAngle = distance * 1.8;
      const randomX = (pseudoRandom(index + 11) - 0.5) * 0.26 * distance;
      const randomY = (pseudoRandom(index + 21) - 0.5) * 0.18 * distance;
      const randomZ = (pseudoRandom(index + 31) - 0.5) * 0.26 * distance;
      const mixedColor = innerColor.clone();

      mixedColor.lerp(midColor, Math.min(distance / (radius * 0.45), 1));
      mixedColor.lerp(outerColor, Math.max((distance - radius * 0.42) / (radius * 0.58), 0));

      positionArray[i3] = Math.cos(branchAngle + spinAngle) * distance + randomX;
      positionArray[i3 + 1] = randomY;
      positionArray[i3 + 2] = Math.sin(branchAngle + spinAngle) * distance + randomZ;

      colorArray[i3] = mixedColor.r;
      colorArray[i3 + 1] = mixedColor.g;
      colorArray[i3 + 2] = mixedColor.b;
    }

    return [positionArray, colorArray];
  }, []);

  useFrame((state) => {
    const elapsed = state.clock.getElapsedTime();

    if (groupRef.current) {
      groupRef.current.rotation.y = elapsed * 0.14;
      groupRef.current.rotation.x = Math.sin(elapsed * 0.24) * 0.11;
    }

    if (pointsRef.current) {
      pointsRef.current.rotation.z = elapsed * 0.08;
    }
  });

  return (
    <group ref={groupRef}>
      <points ref={pointsRef}>
        <bufferGeometry>
          <bufferAttribute attach="attributes-position" args={[positions, 3]} />
          <bufferAttribute attach="attributes-color" args={[colors, 3]} />
        </bufferGeometry>
        <pointsMaterial
          size={0.06}
          vertexColors
          sizeAttenuation
          depthWrite={false}
          transparent
          opacity={0.95}
        />
      </points>
    </group>
  );
};

const OrbitalAccents = () => {
  const haloRef = useRef<Group>(null);
  const outerRingRef = useRef<Mesh>(null);
  const innerRingRef = useRef<Mesh>(null);
  const warmStarRef = useRef<Mesh>(null);
  const coolStarRef = useRef<Mesh>(null);

  useFrame((state) => {
    if (!haloRef.current) {
      return;
    }

    const elapsed = state.clock.getElapsedTime();
    haloRef.current.rotation.y = -elapsed * 0.18;
    haloRef.current.rotation.z = Math.sin(elapsed * 0.34) * 0.1;

    if (outerRingRef.current) {
      outerRingRef.current.position.y = Math.sin(elapsed * 0.65) * 0.08;
      outerRingRef.current.rotation.x = elapsed * 0.14;
    }

    if (innerRingRef.current) {
      innerRingRef.current.position.y = Math.cos(elapsed * 0.85) * 0.06;
      innerRingRef.current.rotation.y = -elapsed * 0.2;
    }

    if (warmStarRef.current) {
      warmStarRef.current.position.y = 0.5 + Math.sin(elapsed * 1.1) * 0.2;
      warmStarRef.current.position.x = 1.4 + Math.cos(elapsed * 0.8) * 0.18;
    }

    if (coolStarRef.current) {
      coolStarRef.current.position.y = -0.8 + Math.cos(elapsed * 1.35) * 0.14;
      coolStarRef.current.position.x = -2.1 + Math.sin(elapsed * 0.9) * 0.16;
    }
  });

  return (
    <group ref={haloRef}>
      <mesh ref={outerRingRef} position={[0, 0, 0]}>
        <torusGeometry args={[4.5, 0.03, 24, 180]} />
        <meshBasicMaterial color="#3ce5ff" transparent opacity={0.28} />
      </mesh>
      <mesh ref={innerRingRef} position={[0, 0, 0]} rotation={[Math.PI / 3.2, 0.2, Math.PI / 5]}>
        <torusGeometry args={[3.25, 0.02, 20, 120]} />
        <meshBasicMaterial color="#ff6b62" transparent opacity={0.34} />
      </mesh>
      <mesh ref={warmStarRef} position={[1.4, 0.5, 1.2]}>
        <sphereGeometry args={[0.18, 24, 24]} />
        <meshBasicMaterial color="#ffd978" />
      </mesh>
      <mesh ref={coolStarRef} position={[-2.1, -0.8, -0.2]}>
        <sphereGeometry args={[0.12, 24, 24]} />
        <meshBasicMaterial color="#78dcff" />
      </mesh>
    </group>
  );
};

const GalaxyScene = () => {
  return (
    <div className="galaxy-scene">
      <Canvas dpr={[1, 1.5]} gl={{ antialias: true, alpha: true }} camera={{ position: [0, 1.2, 9.5], fov: 42 }}>
        <color attach="background" args={['#000000']} />
        <fog attach="fog" args={['#070b14', 8, 18]} />
        <ambientLight intensity={0.6} />
        <directionalLight position={[5, 4, 4]} intensity={1.4} color="#8ad8ff" />
        <pointLight position={[-4, -2, 3]} intensity={7} color="#ff6b62" distance={16} />
        <pointLight position={[3, 2, -2]} intensity={4} color="#8f6dff" distance={14} />
        <ParticleGalaxy />
        <OrbitalAccents />
      </Canvas>
    </div>
  );
};

export default GalaxyScene;
