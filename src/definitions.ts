export interface FileChooserPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
